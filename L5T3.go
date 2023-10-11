package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

type Barrier struct {
	c              int
	n              int
	m              sync.Mutex
	waiting        chan int
	breakCondition chan bool
}

func createBarrier(n int) *Barrier {
	b := Barrier{
		n:              n,
		waiting:        make(chan int, n),
		breakCondition: make(chan bool, n),
	}
	return &b
}

func (b *Barrier) AwaitAndExecuteBarAction(bar_action func() bool, num int) {
	b.m.Lock()
	b.c += 1
	if b.c == b.n {
		result := bar_action()
		for i := 0; i < b.n; i++ {
			b.breakCondition <- result
		}
		b.c = 0
		for i := 0; i < b.n; i++ {
			b.waiting <- 1
		}
	}
	b.m.Unlock()
	<-b.waiting
}

var array_chan chan []uint8

var wait sync.WaitGroup

func sum_check() bool {
	sub_array1 := <-array_chan
	sub_array2 := <-array_chan
	sub_array3 := <-array_chan
	sum1 := 0
	sum2 := 0
	sum3 := 0
	for i := range sub_array1 {
		sum1 += int(sub_array1[i])
	}
	for i := range sub_array2 {
		sum2 += int(sub_array2[i])
	}
	for i := range sub_array3 {
		sum3 += int(sub_array3[i])
	}
	fmt.Println("Сума 1 =", sum1, ", сума 2 =", sum2, ", сума 3 =", sum3)
	if sum1 == sum2 && sum2 == sum3 {
		return true
	}
	return false
}

func array_proccessor(array []uint8, b *Barrier, num int) {
	fmt.Println("Потік", num, "почав роботу")
	for {
		array_chan <- array
		fmt.Println("Потік", num, "прийшов до бар'єру")
		b.AwaitAndExecuteBarAction(sum_check, num)
		if <-b.breakCondition {
			break
		}
		i := rand.Intn(len(array))
		action := rand.Intn(2)
		switch action {
		case 0:
			array[i] += 1
		case 1:
			array[i] -= 1
		}
		time.Sleep(1 * time.Second)
	}
	fmt.Println("Потік", num, "завершив роботу")
	wait.Done()
}

func main() {
	main_array := make([]uint8, 150)
	for i := range main_array {
		main_array[i] = uint8(rand.Intn(4))
	}
	array_chan = make(chan []uint8, 3)
	sub_array1 := main_array[:50]
	sub_array2 := main_array[50:100]
	sub_array3 := main_array[100:]
	br := createBarrier(3)
	wait.Add(3)
	go array_proccessor(sub_array1, br, 1)
	go array_proccessor(sub_array2, br, 2)
	go array_proccessor(sub_array3, br, 3)
	wait.Wait()
	fmt.Println("Програма завершила роботу")
}

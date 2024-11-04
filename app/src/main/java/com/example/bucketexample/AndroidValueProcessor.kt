package com.example.bucketexample

import java.util.LinkedList

class AndroidValueProcessor : ValueProcessor {
    private val queue = LinkedList<Double>()
    private var sum = 0.0
    private var itemCount = 0
    private val allTrips = mutableListOf<List<Double>>()
    private var currentTrip = mutableListOf<Double>()

    override suspend fun init() {
        queue.clear()
        sum = 0.0
        itemCount = 0
        allTrips.clear()
        currentTrip.clear()
    }

    override suspend fun processValue(value: String) {
        itemCount++
        fun processNumber(number: Double) {
            if (sum + number > 3.0) {
                queue.offer(number)
            } else {
                sum += number
                currentTrip.add(number)
                if (sum >= 3.0) {
                    allTrips.add(currentTrip.toList())
                    currentTrip.clear()
                    sum = 0.0
                }
            }
        }

        val inputNumber = value.toDouble()

        while (queue.isNotEmpty() && sum < 3.0) {
            val sizeBefore = queue.size
            val queuedNumber = queue.poll()
            processNumber(queuedNumber)

            if (sizeBefore == queue.size) {
                break
            }
        }

        if (sum < 3.0) {
            processNumber(inputNumber)
        } else {
            queue.offer(inputNumber)
        }

        println("Current sum: $sum, Queue size: ${queue.size}")
    }

    override suspend fun flushRemainingValues(): List<List<Double>> {
        while (queue.isNotEmpty()) {
            val number = queue.poll()
            if (sum + number > 3.0) {
                if (currentTrip.isNotEmpty()) {
                    allTrips.add(currentTrip.toList())
                    currentTrip.clear()
                    sum = 0.0
                }
            }
            currentTrip.add(number)
            sum += number
        }

        if (currentTrip.isNotEmpty()) {
            allTrips.add(currentTrip.toList())
            currentTrip.clear()
            sum = 0.0
        }

        return allTrips.toList()
    }

    override fun getAllProcessedTrips(): List<List<Double>> {
        return allTrips.toList()
    }

    override fun getItemsCount(): Int {
        return itemCount
    }
}

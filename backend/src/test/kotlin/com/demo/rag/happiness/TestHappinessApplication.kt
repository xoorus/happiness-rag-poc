package com.demo.rag.happiness

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<HappinessApplication>().with(TestcontainersConfiguration::class).run(*args)
}

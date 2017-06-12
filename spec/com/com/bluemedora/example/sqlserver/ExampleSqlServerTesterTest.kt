package com.bluemedora.example.sqlserver

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.ShouldSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)

class ExampleSqlServerTesterTest : ShouldSpec() {
    init {
        should("length should return size of string") {
            "hello".length shouldBe 5
        }

    }
}
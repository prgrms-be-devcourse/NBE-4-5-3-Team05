package com.NBE_4_5_2.Team5.global.init

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Locale

@Profile("dev")
@Configuration
class DevInitData {

    @Bean
    fun devApplicationRunner(): ApplicationRunner =
        ApplicationRunner { _: ApplicationArguments? ->
            generateApiJsonFile()
            executeCommand()
        }

    private fun executeCommand() {
        val command = tsGenCommand
        val processBuilder = ProcessBuilder(command)
        processBuilder.redirectErrorStream(true)

        try {
            val process = processBuilder.start()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.lines().forEach { println(it) }
            }
            val exitCode = process.waitFor()
            println("프로세스 종료 코드: $exitCode")
        } catch (e: IOException) {
            throw RuntimeException("명령 실행 중 오류 발생: $command", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("명령 실행 중 오류 발생: $command", e)
        }
    }

    private fun generateApiJsonFile() {
        val filePath = Path.of(API_JSON_FILE)
        val client = HttpClient.newHttpClient()
        try {
            val request = HttpRequest.newBuilder().uri(URI.create(API_URL)).GET().build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                throw RuntimeException("API 요청 실패: HTTP 상태 코드 ${response.statusCode()}")
            }
            Files.writeString(
                filePath,
                response.body(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            println("JSON 데이터가 ${filePath.toAbsolutePath()}에 저장되었습니다.")
        } catch (e: IOException) {
            throw RuntimeException("API JSON 파일 생성 중 오류 발생: $API_JSON_FILE", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("API JSON 파일 생성 중 오류 발생: $API_JSON_FILE", e)
        }
    }

    companion object {
        private const val API_URL = "http://localhost:8080/v3/api-docs/api"
        private const val API_JSON_FILE = "apiV1.json"

        private val tsGenCommand: List<String>
            get() {
                val os = System.getProperty("os.name").lowercase(Locale.getDefault())
                return if (os.contains("win")) {
                    listOf(
                        "cmd.exe",
                        "/c",
                        "npx --package typescript --package openapi-typescript --package punycode openapi-typescript $API_JSON_FILE -o ../frontend/src/lib/backend/apiV1/schema.d.ts"
                    )
                } else {
                    listOf(
                        "sh",
                        "-c",
                        "npx --package typescript --package openapi-typescript --package punycode openapi-typescript $API_JSON_FILE -o ../frontend/src/lib/backend/apiV1/schema.d.ts"
                    )
                }
            }
    }
}

import java.io.File

fun findCommand(input: String): Boolean {
    val parts = input.split("\\s+".toRegex())
    val command = parts[0]
    val args = parts.drop(1)
    return when (command){
        "ls" -> {
            println("ls ${args.joinToString(" ")}")
            true
        }
        "cd" -> {
            println("cd ${args.joinToString(" ")}")
            true
        }
        "exit" -> true
        else -> {
            println("Unknown command: $command")
            false
        }
    }
}

fun main(args: Array<String>) {
    var vfsPart: String? = null
    var scrPart: String? = null

    for (i in args.indices) {
        if (args[i] == "--vfs" && i+1 < args.size) {
            vfsPart = args[i+1]
        }
        if (args[i] == "--script" && i+1 < args.size) {
            scrPart = args[i+1]
        }
    }
    println("[DEBUG] VFS path: ${vfsPart ?: "не задан"}")
    println("[DEBUG] Script path: ${scrPart ?: "не задан"}")

    if (scrPart != null) {
        val file = File(scrPart)
        if (file.exists()) {
            val lines = file.readLines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                println("vrs> $trimmed")

                if (trimmed == "exit") continue

                val command = findCommand(trimmed)
                if (!command){
                    println("[ERROR] Invalid command: $command")
            }
        }
        } else println("[ERROR] Файл не найден")
    }

    while (true) {
        print("vfs> ")
        val input = readlnOrNull()?.trim()

        if (input == "exit") break
        else if (input.isNullOrEmpty()) continue

        findCommand(input)

    }
}
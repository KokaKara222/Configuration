import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class ReadZipFile (private val zipPath: String) {
    private val filesMemory =mutableSetOf<String>() //хранилище всех путей файлов
    var currentPath: String ="/" //хранит текущую папку
        private set

    var LoadSuccess: Boolean =false // флаг успешной загрузка
        private set

    init{
        loadZipArchive()
    }

    private fun loadZipArchive(){
        if (zipPath.isNullOrEmpty()) {
            println("[ERROR VFS] Путь к VFS не указан.")
            return
        }

        val zipFile = File(zipPath)
        if (!zipFile.exists() || !zipFile.isFile) {
            println("[ERROR VFS] Файл VFS не найден по пути: $zipPath")
            return
        }

        try{
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    var name = entry.name.replace('\\', '/')
                    if (!name.startsWith("/")) {
                        name = "/$name"
                    }
                    filesMemory.add(name)

                    if (entry.isDirectory && !name.endsWith("/")) {
                        filesMemory.add("$name/")
                    }

                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            filesMemory.add("/")
            LoadSuccess = true
            println("[INFO VFS] VFS успешно загружена из $zipPath. Загружено элементов: ${filesMemory.size}")
        } catch (e: Exception) {
            println("[ERROR VFS] Неверный формат VFS или ошибка чтения архива: ${e.message}")
        }
    }

    fun ls(args: String): Boolean{
        val targetPath = if (args.isEmpty()) currentPath else resolvePath(args)
        val formattedPath = if(targetPath.endsWith("/")) targetPath else "$targetPath/"
        if(!filesMemory.contains(formattedPath)&& formattedPath != "/"){
            println("ls: $args: No such file or directory")
            return false
        }
        val items = filesMemory.filter{ path->
            path.startsWith(formattedPath) && path != formattedPath
        }.map { path ->
            val relative = path.removePrefix(formattedPath)
            relative.split("/").first()
        }.distinct().sorted()
        if (items.isNotEmpty()){
            println(items.joinToString(" "))
        }
        return true
    }

    fun cd(args: String): Boolean{
        if (args.isEmpty() || args== "~") {
            currentPath = "/"
            return true
        }
        val targetPath = if (args.isEmpty()) currentPath else resolvePath(args)
        val formattedPath = if(targetPath.endsWith("/")) targetPath else "$targetPath/"
        if (filesMemory.contains(formattedPath) || formattedPath != "/") {
            currentPath = formattedPath
            return true
        } else{
            println("cd: $args: No such file or directory")
            return false
        }
    }

    fun resolvePath(relativePath: String): String{
        val base = if (relativePath.startsWith("/")) "/" else currentPath
        val parts = "$base/$relativePath".split("/").filter { it.isNotEmpty()&& it !="." }

        val stack = mutableListOf<String>()
        for (part in parts){
            if (part==".."){
                if (stack.isNotEmpty()) stack.removeAt(stack.size -1)
            }else{
                stack.add(part)
            }

        }
        return "/" +stack.joinToString("/")
    }
}
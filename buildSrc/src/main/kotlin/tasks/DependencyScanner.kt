package tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

// Custom task definition
open class FindDependenciesTask : DefaultTask() {
    @TaskAction
    fun findDependencies() {
        val inputClassName = project.findProperty("inputClassName") as? String
            ?: throw IllegalArgumentException("Please provide an input class name using -PinputClassName=YourClassName")

        val srcDir = project.file("src/main/kotlin")
        val kotlinFiles = srcDir.walkTopDown().filter { it.extension == "kt" }.toList()

        val classDependencies = mutableMapOf<String, MutableList<String>>()

        kotlinFiles.forEach { file ->
            val classDependenciesInFile = analyzeKotlinFile(file)
            classDependencies.putAll(classDependenciesInFile)
        }

        if (classDependencies.containsKey(inputClassName)) {
            println("Classes using fields from '$inputClassName':")
            classDependencies[inputClassName]?.forEach { dependentClass ->
                println(" - $dependentClass")
            }
        } else {
            println("Class '$inputClassName' not found or no dependencies.")
        }
    }

    private fun analyzeKotlinFile(file: File): Map<String, MutableList<String>> {
        val classDependencies = mutableMapOf<String, MutableList<String>>()

        val fileContent = file.readText()

        val classPattern = Regex("""class\s+(\w+)""")
        val propertyPattern = Regex("""val\s+(\w+)|var\s+(\w+)""")
        val methodPattern = Regex("""fun\s+(\w+)""")

        val classesInFile = classPattern.findAll(fileContent).map { it.groupValues[1] }.toList()

        classesInFile.forEach { className ->
            val properties = propertyPattern.findAll(fileContent).map { it.groupValues[1] }.toList()
            val methods = methodPattern.findAll(fileContent).map { it.groupValues[1] }.toList()

            val allMembers = properties + methods

            allMembers.forEach { member ->
                if (fileContent.contains("$className.$member")) {
                    classDependencies.computeIfAbsent(className) { mutableListOf() }.add(member)
                }
            }
        }

        return classDependencies
    }
}

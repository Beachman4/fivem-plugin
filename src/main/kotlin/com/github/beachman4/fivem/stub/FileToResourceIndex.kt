package com.github.beachman4.fivem.stub

import com.intellij.util.indexing.* // ktlint-disable no-wildcard-imports
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import java.io.DataInput
import java.io.DataOutput

class FileToResourceIndex : FileBasedIndexExtension<String, String>() {

    companion object {
        val KEY = ID.create<String, String>("com.beachman4.fivem.fivem_file_to_resource")
    }

    override fun getValueExternalizer() = object : DataExternalizer<String> {
        override fun save(out: DataOutput, value: String?) {
            out.writeUTF(value!!)
        }

        override fun read(input: DataInput): String {
            return input.readUTF()
        }
    }

    override fun getName(): ID<String, String> {
        return KEY
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return false
    }

    @Suppress("LongMethod", "ComplexMethod")
    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        return DataIndexer { fileContent: FileContent ->
            val map = mutableMapOf<String, String>()

            if (fileContent.file.parent == null) {
                return@DataIndexer map
            }

            var file = fileContent.file.parent
            var previous = fileContent.file
            var found = false

            var i = 0
            @Suppress("MagicNumber")
            val max = 20

            while (!found) {
                i++

                if (i > max) {
                    file = null
                    break
                }

                if (file == null) {
                    break
                }

                var children = file.children

                for (child in children) {
                    if (child.name == "fxmanifest.lua" || child.name == "__resource.lua") {
                        found = true
                    }
                }

                if (!found) {
                    if ((file.name.startsWith("[") && file.name.endsWith("]")) || file.name.equals("resources")) {
                        file = previous
                        found = true
                    } else {
                        previous = file
                        file = file.parent
                    }
                }
            }

            if (file != null) {
                map["resource"] = file.name
            }

            return@DataIndexer map
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file ->
            val extension = file.extension
            val validTypes = listOf(
                "lua",
                "meta",
                "xml",
                "json",
                "yft",
                "html",
                "js",
                "css",
                "tff",
                "ytd",
                "ymf",
                "ytyp",
                "ybn",
                "ymap",
                "ydr"
            )

            return@InputFilter validTypes.contains(extension)
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }
}

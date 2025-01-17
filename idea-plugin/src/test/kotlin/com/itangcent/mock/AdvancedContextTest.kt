package com.itangcent.mock

import com.itangcent.common.spi.Setup
import com.itangcent.idea.plugin.rule.SuvRuleParser
import com.itangcent.idea.swing.MessagesHelper
import com.itangcent.intellij.config.AbstractConfigReader
import com.itangcent.intellij.config.ConfigReader
import com.itangcent.intellij.config.rule.RuleParser
import com.itangcent.intellij.context.ActionContext
import com.itangcent.intellij.extend.guice.PostConstruct
import com.itangcent.intellij.extend.guice.singleton
import com.itangcent.intellij.extend.guice.with
import com.itangcent.intellij.file.AbstractLocalFileRepository
import com.itangcent.intellij.file.LocalFileRepository
import com.itangcent.intellij.jvm.PsiClassHelper
import com.itangcent.intellij.psi.DefaultPsiClassHelper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path


/**
 * BaseContextTest with [tempDir]
 */
abstract class AdvancedContextTest : EasyBaseContextTest() {

    @JvmField
    @TempDir
    var tempDir: Path? = null

    open fun customConfig(): String? {
        return null
    }

    override fun bind(builder: ActionContext.ActionContextBuilder) {
        super.bind(builder)
        builder.bindInstance("plugin.name", "easy_api")

        TempFileRepository().let { rep ->
            builder.bind(LocalFileRepository::class) {
                it.toInstance(rep)
            }
            builder.bind(LocalFileRepository::class, "projectCacheRepository") {
                it.toInstance(rep)
            }
        }

        customConfig()?.takeIf { it.isNotBlank() }
            ?.let { config ->
                builder.bind(ConfigReader::class) {
                    it.toInstance(ConfigReaderAdaptor(config))
                }
            }

        builder.bind(RuleParser::class) { it.with(SuvRuleParser::class).singleton() }
        builder.bind(PsiClassHelper::class) { it.with(DefaultPsiClassHelper::class).singleton() }
        builder.bind(MessagesHelper::class) { it.with(EmptyMessagesHelper::class).singleton() }
    }

    private inner class TempFileRepository : AbstractLocalFileRepository() {
        override fun basePath(): String {
            return tempDir.toString()
        }
    }

    private inner class ConfigReaderAdaptor(val config: String) : AbstractConfigReader() {

        @PostConstruct
        fun init() {
            loadConfigInfoContent(config, "properties")
        }

        override fun findConfigFiles(): List<String>? {
            return null
        }
    }

    protected val n = System.getProperty("line.separator") ?: "\n"
    protected val s = File.separator!!

    companion object {
        @JvmStatic
        @BeforeAll
        fun load() {
            Setup.load()
        }
    }
}
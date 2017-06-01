package inject

import dagger.Component
import dagger.internal.DaggerCollections
import domain.shared.JADB
import javafx.application.Application
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {
    fun inject(jadb: JADB)

    companion object {
        fun init(application: Application) : AppComponent {
            return DaggerAppComponent
        }
    }
}
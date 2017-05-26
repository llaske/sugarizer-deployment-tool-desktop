import javafx.stage.Stage
import tornadofx.App
import views.MainView

class MainApp() : App(MainView::class) {
    override fun start(stage: Stage) {
        super.start(stage)

        stage.isResizable = false
        stage.height = 480.0
        stage.width = 800.0
    }
}
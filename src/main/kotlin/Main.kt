import javafx.stage.Stage
import domain.shared.JADB
import tornadofx.App
import view.main.MainView
import javax.inject.Inject

class MainApp  : App(MainView::class) {
    @Inject val jadb = JADB()

    override fun start(stage: Stage) {
        super.start(stage)

        stage.isResizable = false
        stage.height = 480.0
        stage.width = 800.0
    }

    override fun stop() {
        jadb.stopADB()

        super.stop()

        println("STOP")
    }
}
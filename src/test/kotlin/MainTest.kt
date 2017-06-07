import com.sugarizer.domain.shared.JADB
import com.sugarizer.inject.AppComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import tornadofx.App
import javax.inject.Inject

class MainTest {
    @get:Rule val rule = RuleTest()

    @Mock lateinit var jadb: JADB

    @Test
    fun testSomething(){
        Mockito.`when`(jadb.numberDevice()).thenReturn(0)

        val number = jadb.numberDevice()

        assertEquals(number, 0)
    }
}
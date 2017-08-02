import com.sugarizer.utils.shared.JADB
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class MainTest {
    val jadb: JADB = mock(JADB::class.java)

    @Test
    fun testSomething() {
        Mockito.doReturn(Int.MAX_VALUE).`when`(jadb).numberDevice()

        val number = jadb.numberDevice()

        assertEquals(number, Int.MAX_VALUE)
    }
}
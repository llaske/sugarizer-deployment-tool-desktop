import com.sugarizer.domain.shared.JADB
import com.sugarizer.inject.AppComponent
import com.sugarizer.inject.AppModule
import com.sugarizer.main.Main
import it.cosenonjaviste.daggermock.DaggerMockRule
import junit.framework.TestCase
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify

class RuleTest : DaggerMockRule<AppComponent>(AppComponent::class.java, AppModule()) {
    init {
        providesMock(JADB::class.java)
    }
}
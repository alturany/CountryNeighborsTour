import org.junit.Test;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MoneyTest {

    @Test
    public void testMoneyApi() {
        MonetaryAmount eurAmount1 = Monetary.getDefaultAmountFactory().setNumber(1.1111).setCurrency("EUR").create();
        MonetaryAmount eurAmount2 = Monetary.getDefaultAmountFactory().setNumber(1.1141).setCurrency("EUR").create();

        MonetaryAmount eurAmount3 = eurAmount1.add(eurAmount2);
        assertThat(eurAmount3.toString(), is("EUR 2.2252"));

        MonetaryRounding defaultRounding = Monetary.getDefaultRounding();
        MonetaryAmount eurAmount4 = eurAmount3.with(defaultRounding);
        assertThat(eurAmount4.toString(), is("EUR 2.23"));

        MonetaryAmountFormat germanFormat = MonetaryFormats.getAmountFormat(Locale.GERMAN);
        assertThat(germanFormat.format(eurAmount4), is("EUR 2,23") );
    }
}
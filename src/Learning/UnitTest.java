package Learning;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnitTest {

	QTable testTable;
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	public void testPrintTable() throws IOException {
		testTable.printTable("printTable.csv");
	}

}

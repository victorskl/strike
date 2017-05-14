package strike;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import strike.common.model.ServerInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ConfigTest {

    private File config = new File("./server.tab");
    private CSVReader reader;

    @Before
    public void before() throws FileNotFoundException {
        reader = new CSVReader(new FileReader(config), '\t');
    }

    @Test @Ignore
    public void testReadConfig() throws IOException {
        String[] record;
        while ((record = reader.readNext()) != null) {
            for (String value : record) {
                System.out.println(value);
            }
        }
    }

    @Test @Ignore
    public void testReadConfigBean() throws FileNotFoundException {
        ColumnPositionMappingStrategy<ServerInfo> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(ServerInfo.class);
        CsvToBean<ServerInfo> csvToBean = new CsvToBean<>();
        List<ServerInfo> serverInfoList = csvToBean.parse(strategy, reader);
        for (ServerInfo info : serverInfoList) {
            System.out.print(info.getServerId());
            System.out.print("\t");
            System.out.print(info.getAddress());
            System.out.print("\t");
            System.out.print(info.getPort());
            System.out.print("\t");
            System.out.print(info.getManagementPort());
            System.out.print("\n");
        }
    }
}

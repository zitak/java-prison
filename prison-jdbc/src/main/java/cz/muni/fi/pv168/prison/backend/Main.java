package cz.muni.fi.pv168.prison.backend;

import cz.muni.fi.pv168.common.IllegalEntityException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.List;

public class Main {

    final static Logger log = LoggerFactory.getLogger(Main.class);

    public static DataSource createMemoryDatabase() {
        BasicDataSource bds = new BasicDataSource();
        //set JDBC driver and URL
        bds.setDriverClassName(EmbeddedDriver.class.getName());
        bds.setUrl("jdbc:derby:memory:prisonDB2;create=true");
        //populate db with tables and data
        new ResourceDatabasePopulator(
                new ClassPathResource("schema-javadb.sql"),
                new ClassPathResource("test-data.sql"))
                .execute(bds);
        return bds;
    }

    public static void main(String[] args) throws IllegalEntityException {

        log.info("zaciname");

        DataSource dataSource = createMemoryDatabase();
        PrisonerManagerImpl prisonerManager = new PrisonerManagerImpl(Clock.systemDefaultZone());
        prisonerManager.setDataSource(dataSource);

        List<Prisoner> allPrisoners = prisonerManager.findAllPrisoners();
        System.out.println("allPrisoners = " + allPrisoners);

    }

}
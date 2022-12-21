package localtests;

import dependency_injection.BeanFactory;
import dependency_injection.BeanFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

public class SelfTest {
    private BeanFactory beanFactory;

    @BeforeEach
    public void setup() {
        beanFactory = new BeanFactoryImpl();
    }

    @Test
    public void testLoadValueProperties() {
        File file = new File("local-value.properties");
        beanFactory.loadValueProperties(file);
        System.out.println();
    }

    @Test
    public void testLoadInjectProperties() {
        File file = new File("local-inject.properties");
        beanFactory.loadInjectProperties(file);
        System.out.println();
    }

}

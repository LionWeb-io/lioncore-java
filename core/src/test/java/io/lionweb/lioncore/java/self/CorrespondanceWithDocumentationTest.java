package io.lionweb.lioncore.java.self;

import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.serialization.JsonSerialization;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CorrespondanceWithDocumentationTest {





    @Test
    public void lioncoreIsTheSameAsInTheOrganizationRepo() throws IOException {
        JsonSerialization jsonSer = JsonSerialization.getStandardSerialization();

        URL url = new URL("https://raw.githubusercontent.com/LIonWeb-org/organization/niko/update-docs-june2/lioncore/metametamodel/lioncore.json");
         List<Node> nodes = jsonSer.unserializeToNodes(url);
//        File file = new File("/Users/ftomassetti/Downloads/lioncore.json");
//        List<Node> nodes = jsonSer.unserializeToNodes(file);

        Language unserializedLioncore = (Language) nodes.get(0);
    }

    @Test
    public void builtInIsTheSameAsInTheOrganizationRepo() throws FileNotFoundException {
        JsonSerialization jsonSer = JsonSerialization.getStandardSerialization();

        //URL url = new URL("https://raw.githubusercontent.com/LIonWeb-org/organization/niko/update-docs-june2/lioncore/metametamodel/builtins.json");
        //String content = getStringFromUrl(url);
        // List<Node> nodes = jsonSer.unserializeToNodes(content);
        File file = new File("/Users/ftomassetti/Downloads/builtins.json");
        List<Node> nodes = jsonSer.unserializeToNodes(file);

        Language unserializedBuiltins = (Language) nodes.get(0);
    }
}

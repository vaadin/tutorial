import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

/**
 * Generates the application of the Vaadin tutorial as it is at the end of each step, from the code
 * listings in vaadin/docs.
 * <p>
 * Usage: {@code java GenerateSteps.java <docs-dir> <output-dir>}
 * <p>
 * {@code <docs-dir>} is a checkout of vaadin/docs. The first step, Set Up Project, downloads the
 * project skeleton that the step links to, and adds the dependencies and SQL files that the step
 * lists. Every later step starts from the previous one, adds the dependencies that the step lists,
 * and writes the files of its "Complete Code for This Step" section. Each step goes in
 * {@code <output-dir>/<page>}, where {@code <page>} is the name of the step's page, such as
 * {@code list-data}. The file {@code <output-dir>/steps.tsv} lists the pages and their titles, in
 * the order of the tutorial.
 */
public class GenerateSteps {

    static final String TUTORIAL_DIR = "articles/getting-started/tutorial";
    static final String VERSION_FILE = "articles/_vaadin-version.adoc";
    static final String DOCS_URL = "https://vaadin.com/docs/latest/getting-started/tutorial";
    static final String REPOSITORY_URL = "https://github.com/vaadin/tutorial";

    static final Pattern ATTRIBUTE = Pattern.compile("^:([\\w-]+):\\s*(.*)$");
    static final Pattern ATTRIBUTE_REFERENCE = Pattern.compile("\\{([\\w-]+)}");
    static final Pattern SKELETON_LINK = Pattern.compile("link:(https://start\\.vaadin\\.com/skeleton\\?[^\\[]+)\\[");
    static final Pattern SQL_LINK = Pattern.compile("link:\\{articles}/([\\w/-]+\\.sql)\\[");
    static final Pattern START_PROJECT_COMMENT = Pattern.compile("(?m)^[ \\t]*<!-- Project from https://start\\.vaadin\\.com/\\S* -->\\R");
    static final Pattern LISTING_TITLE = Pattern.compile("^\\.([\\w.-]+\\.\\w+)$");
    static final Pattern SOURCE_BLOCK = Pattern.compile("^\\[source,(\\w+).*]$");
    static final Pattern CALLOUT = Pattern.compile("(?m)[ \\t]*(?://|#) <\\d+>[ \\t]*$|[ \\t]*/\\* <\\d+> \\*/[ \\t]*$|[ \\t]*<!-- <\\d+> -->[ \\t]*$");
    static final Pattern DEPENDENCY = Pattern.compile("(?s)<dependency>.*?</dependency>");
    static final Pattern GROUP_ID = Pattern.compile("<groupId>([^<]+)</groupId>");
    static final Pattern ARTIFACT_ID = Pattern.compile("<artifactId>([^<]+)</artifactId>");
    static final Pattern PACKAGE = Pattern.compile("(?m)^package ([\\w.]+);");

    // The Set Up Project step tells the reader to save the SQL files in this directory.
    static final String SQL_DIR = "src/main/resources";
    // Files linked with {articles} are served from this directory of vaadin/docs.
    static final String DOCS_RESOURCES_DIR = "src/main/resources/META-INF/resources";

    record Step(String page, String title, int order, List<String> lines) {

        String url() {
            return DOCS_URL + "/" + page;
        }

        String branch() {
            return "step/" + page;
        }
    }

    record Listing(String fileName, String language, String code) {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java GenerateSteps.java <docs-dir> <output-dir>");
            System.exit(2);
        }
        var docs = Path.of(args[0]);
        var output = Path.of(args[1]);
        if (Files.exists(output)) {
            try (var files = Files.list(output)) {
                if (files.findAny().isPresent()) {
                    throw new IllegalArgumentException(output + " is not empty");
                }
            }
        }

        var attributes = readAttributes(docs.resolve(VERSION_FILE));
        var steps = readSteps(docs.resolve(TUTORIAL_DIR));
        var tsv = new StringBuilder();
        Path previous = null;
        for (int i = 0; i < steps.size(); i++) {
            var step = steps.get(i);
            var next = i + 1 < steps.size() ? steps.get(i + 1) : null;
            var project = output.resolve(step.page());
            System.out.println("Generating " + step.title() + " in " + project);
            if (previous == null) {
                setUpProject(step, project, docs, attributes);
            } else {
                copyProject(previous, project);
                addDependencies(step, project);
                writeCompleteCode(step, project);
            }
            writeReadme(step, next, project, attributes);
            tsv.append(step.page()).append('\t').append(step.title()).append('\n');
            previous = project;
        }
        Files.writeString(output.resolve("steps.tsv"), tsv);
    }

    static Map<String, String> readAttributes(Path file) throws IOException {
        var attributes = new HashMap<String, String>();
        for (var line : Files.readAllLines(file)) {
            var matcher = ATTRIBUTE.matcher(line);
            if (matcher.matches()) {
                attributes.put(matcher.group(1), matcher.group(2).trim());
            }
        }
        return attributes;
    }

    static List<Step> readSteps(Path tutorial) throws IOException {
        var steps = new ArrayList<Step>();
        try (var files = Files.list(tutorial)) {
            for (var file : files.filter(f -> f.toString().endsWith(".adoc")).toList()) {
                var page = file.getFileName().toString().replaceFirst("\\.adoc$", "");
                if (page.equals("index")) {
                    continue;
                }
                var lines = Files.readAllLines(file);
                var frontMatter = readFrontMatter(lines, file);
                steps.add(new Step(page, frontMatter.get("title"), Integer.parseInt(frontMatter.get("order")), lines));
            }
        }
        steps.sort(Comparator.comparingInt(Step::order));
        return steps;
    }

    static Map<String, String> readFrontMatter(List<String> lines, Path file) {
        if (lines.isEmpty() || !lines.getFirst().equals("---")) {
            throw new IllegalStateException(file + " has no front matter");
        }
        var frontMatter = new HashMap<String, String>();
        for (var line : lines.subList(1, lines.size())) {
            if (line.equals("---")) {
                if (!frontMatter.containsKey("title") || !frontMatter.containsKey("order")) {
                    throw new IllegalStateException(file + " has no title or order");
                }
                return frontMatter;
            }
            var colon = line.indexOf(':');
            if (colon > 0) {
                frontMatter.put(line.substring(0, colon).trim(), line.substring(colon + 1).trim());
            }
        }
        throw new IllegalStateException(file + " has unterminated front matter");
    }

    // Set Up Project

    static void setUpProject(Step step, Path project, Path docs, Map<String, String> attributes) throws Exception {
        var text = String.join("\n", step.lines());
        var link = SKELETON_LINK.matcher(text);
        if (!link.find()) {
            throw new IllegalStateException(step.page() + " doesn't link to a project skeleton");
        }
        var url = resolveAttributes(link.group(1), attributes);
        System.out.println("  Downloading " + url);
        downloadSkeleton(URI.create(url), project);

        var pom = project.resolve("pom.xml");
        Files.writeString(pom, START_PROJECT_COMMENT.matcher(Files.readString(pom)).replaceAll(""));
        addDependencies(step, project);

        var sqlFiles = SQL_LINK.matcher(text);
        int count = 0;
        while (sqlFiles.find()) {
            var source = docs.resolve(DOCS_RESOURCES_DIR).resolve(sqlFiles.group(1));
            var target = project.resolve(SQL_DIR).resolve(source.getFileName().toString());
            System.out.println("  Copying " + source.getFileName() + " to " + project.relativize(target));
            Files.copy(source, target);
            count++;
        }
        if (count == 0) {
            throw new IllegalStateException(step.page() + " doesn't link to any SQL files");
        }
    }

    static String resolveAttributes(String text, Map<String, String> attributes) {
        return ATTRIBUTE_REFERENCE.matcher(text).replaceAll(match -> {
            var value = attributes.get(match.group(1));
            if (value == null) {
                throw new IllegalStateException("Unknown attribute " + match.group());
            }
            return Matcher.quoteReplacement(value);
        });
    }

    static void downloadSkeleton(URI uri, Path project) throws Exception {
        var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        var response = client.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            throw new IOException("Downloading " + uri + " failed with status " + response.statusCode());
        }
        Files.createDirectories(project);
        try (var zip = new ZipInputStream(response.body())) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                // The skeleton is in a directory of its own, and comes with a Git repository.
                var name = entry.getName().replaceFirst("^[^/]+/", "");
                if (name.isEmpty() || name.equals(".git/") || name.startsWith(".git/")) {
                    continue;
                }
                var target = project.resolve(name).normalize();
                if (!target.startsWith(project)) {
                    throw new IOException("Unexpected entry " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zip, target);
                    makeScriptExecutable(target);
                }
            }
        }
    }

    // Java's zip support doesn't expose file permissions. The Maven wrapper is the only script in the skeleton.
    static void makeScriptExecutable(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            var start = in.readNBytes(2);
            if (start.length == 2 && start[0] == '#' && start[1] == '!') {
                if (!file.toFile().setExecutable(true, false)) {
                    throw new IOException("Can't make " + file + " executable");
                }
            }
        }
    }

    // Later steps

    static void copyProject(Path from, Path to) throws IOException {
        try (Stream<Path> files = Files.walk(from)) {
            for (var source : files.toList()) {
                var target = to.resolve(from.relativize(source).toString());
                if (Files.isDirectory(source)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }
    }

    // Adds the dependencies of the step's XML listings to the project's dependencies, unless the
    // project already has them. They go after the last dependency that isn't scoped for tests.
    static void addDependencies(Step step, Path project) throws IOException {
        var pomFile = project.resolve("pom.xml");
        var pom = Files.readString(pomFile);
        for (var listing : listings(step.lines(), false)) {
            if (!listing.language().equals("xml")) {
                continue;
            }
            var dependencies = DEPENDENCY.matcher(listing.code());
            while (dependencies.find()) {
                var dependency = dependencies.group();
                var id = dependencyId(dependency);
                int start = pom.indexOf("<dependencies>", Math.max(0, pom.indexOf("</dependencyManagement>")));
                int end = pom.indexOf("</dependencies>", start);
                if (start < 0 || end < 0) {
                    throw new IllegalStateException("pom.xml has no dependencies");
                }
                var existing = DEPENDENCY.matcher(pom).region(start, end);
                int insertAt = -1;
                boolean found = false;
                while (existing.find()) {
                    found |= dependencyId(existing.group()).equals(id);
                    if (!existing.group().contains("<scope>test</scope>")) {
                        insertAt = existing.end();
                    }
                }
                if (found) {
                    continue;
                }
                if (insertAt < 0) {
                    insertAt = start + "<dependencies>".length();
                }
                var lineStart = pom.lastIndexOf('\n', start) + 1;
                var indent = pom.substring(lineStart, start) + "    ";
                var lines = new StringBuilder();
                for (var line : dependency.split("\n")) {
                    lines.append('\n').append(indent).append(line);
                }
                System.out.println("  Adding dependency " + id);
                pom = pom.substring(0, insertAt) + lines + pom.substring(insertAt);
            }
        }
        Files.writeString(pomFile, pom);
    }

    static String dependencyId(String dependency) {
        var groupId = GROUP_ID.matcher(dependency);
        var artifactId = ARTIFACT_ID.matcher(dependency);
        if (!groupId.find() || !artifactId.find()) {
            throw new IllegalStateException("Dependency without groupId or artifactId: " + dependency);
        }
        return groupId.group(1) + ":" + artifactId.group(1);
    }

    static void writeCompleteCode(Step step, Path project) throws IOException {
        var listings = listings(completeCodeSection(step), true);
        if (listings.isEmpty()) {
            throw new IllegalStateException(step.page() + " has no complete code");
        }
        for (var listing : listings) {
            var target = targetFile(listing, project);
            System.out.println("  Writing " + project.relativize(target));
            Files.createDirectories(target.getParent());
            Files.writeString(target, listing.code());
        }
    }

    static List<String> completeCodeSection(Step step) {
        var lines = step.lines();
        int start = lines.indexOf("== Complete Code for This Step");
        if (start < 0) {
            throw new IllegalStateException(step.page() + " has no Complete Code for This Step section");
        }
        int end = start + 1;
        while (end < lines.size() && !lines.get(end).startsWith("== ")) {
            end++;
        }
        return lines.subList(start + 1, end);
    }

    // Returns the source blocks in the lines. If titled is true, every block must have a title
    // that names the file, such as ".ProductCatalogView.java".
    static List<Listing> listings(List<String> lines, boolean titled) {
        var listings = new ArrayList<Listing>();
        String title = null;
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            var titleMatcher = LISTING_TITLE.matcher(line);
            if (titleMatcher.matches()) {
                title = titleMatcher.group(1);
                continue;
            }
            var source = SOURCE_BLOCK.matcher(line);
            if (!source.matches()) {
                continue;
            }
            if (i + 1 >= lines.size() || !lines.get(i + 1).equals("----")) {
                throw new IllegalStateException("Source block without ---- delimiter: " + line);
            }
            int end = lines.subList(i + 2, lines.size()).indexOf("----");
            if (end < 0) {
                throw new IllegalStateException("Unterminated source block after " + title);
            }
            var code = String.join("\n", lines.subList(i + 2, i + 2 + end)) + "\n";
            if (titled && title == null) {
                throw new IllegalStateException("Source block without a file name: " + code);
            }
            listings.add(new Listing(title, source.group(1), CALLOUT.matcher(code).replaceAll("")));
            title = null;
            i += end + 2;
        }
        return listings;
    }

    // A Java class goes in the directory of its package, and a test in the test sources. Any
    // other file replaces the only file of the same name in the project.
    static Path targetFile(Listing listing, Path project) throws IOException {
        if (listing.fileName().endsWith(".java")) {
            var className = listing.fileName().replaceFirst("\\.java$", "");
            if (!Pattern.compile("\\b(class|interface|record|enum)\\s+" + className + "\\b").matcher(listing.code()).find()) {
                throw new IllegalStateException(listing.fileName() + " doesn't declare " + className);
            }
            var packageName = PACKAGE.matcher(listing.code());
            if (!packageName.find()) {
                throw new IllegalStateException(listing.fileName() + " has no package");
            }
            var sources = listing.code().contains("import org.junit.") ? "src/test/java" : "src/main/java";
            return project.resolve(sources).resolve(packageName.group(1).replace('.', '/')).resolve(listing.fileName());
        }
        try (Stream<Path> files = Files.walk(project)) {
            var matches = files.filter(f -> f.getFileName().toString().equals(listing.fileName())).toList();
            if (matches.size() != 1) {
                throw new IllegalStateException("Expected one " + listing.fileName() + " in the project, found " + matches);
            }
            return matches.getFirst();
        }
    }

    static void writeReadme(Step step, Step next, Path project, Map<String, String> attributes) throws IOException {
        var readme = new StringBuilder();
        readme.append("# Vaadin Tutorial: ").append(step.title()).append("\n\n");
        readme.append("This branch holds the application of the [Vaadin tutorial](").append(DOCS_URL)
                .append(") as it is at the end of the [").append(step.title()).append("](").append(step.url())
                .append(") step.");
        if (next != null) {
            readme.append(" To continue the tutorial from here, follow the [").append(next.title()).append("](")
                    .append(next.url()).append(") step. The [`").append(next.branch()).append("`](")
                    .append(REPOSITORY_URL).append("/tree/").append(next.branch())
                    .append(") branch holds the result.\n\n");
        } else {
            readme.append(" This is the last step, so this branch holds the finished application. The [`main`](")
                    .append(REPOSITORY_URL).append(") branch has the same code.\n\n");
        }
        readme.append("To run the application, you need a Java Development Kit (JDK), version ")
                .append(attributes.getOrDefault("vaadin-start-java-version", "21"))
                .append(" or newer. Start the application with `./mvnw` (`mvnw` on Windows), and open http://localhost:8080 in your browser. ")
                .append("To build the application for production and run its tests, run `./mvnw verify`.\n\n");
        readme.append("This branch is generated from the code listings of the tutorial, and is replaced when they change. ")
                .append("To fix the code, fix the tutorial in [vaadin/docs](https://github.com/vaadin/docs). ")
                .append("The [README of the `main` branch](").append(REPOSITORY_URL)
                .append("#step-branches) explains how the branches are generated.\n");
        Files.writeString(project.resolve("README.md"), readme);
    }
}

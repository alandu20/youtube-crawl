/*********************************************************************************
  * Author: Alan Du
  * 
  * Description: Starting from initial Youtube video URL, crawls for related videos
  * from sidebar. Filters out non-HD (< 720p) videos & videos with less than a user-
  * defined number of views. Writes video metadata to tab-delimited CSV file (file
  * name user-defined): URL, Title, Description, Tags, and View Count. Program ends
  * after user-defined number of videos are written to CSV file. At end, prints
  * number of vids printed, number of vids crawled, and number of seconds elapsed.
  * 
  * Compilation: javac YoutubeCrawler.java
  * Dependencies: StdOut.java, In.java, Queue.java
  * 
  * Execution:
  * java YoutubeCrawler Youtube_URL min_#_views csv_filename #_vids_crawled
  * Sample Execution:
  * java YoutubeCrawler https:///www.youtube.com/watch?v=pHKz2bU5dbU 100000 filename.csv 250
  * 
  * Miscellaneous:
  * Regex to find Youtube videos in page source -> /watch\\?v=((\\w){11})
  *  works b/c all Youtube URLs end in v = some 11 char code
  * Data Scraping (using Pattern & Matcher):
  *  (note: added \ before " to meet java language requirement)
  * 1. Title -> og:title\" content=\"
  * 2. Description (abbreviated) -> og:description\" content=\"
  *     for full description, which has inconsistent formatting -> eow-description" >
  * 3. Tags -> og:video:tag\" content=\"
  * 4. View Count -> \"view_count\":\"
  * (note: changes special HTML character codes &#39; and &quot and &amp; to apostrophe
  * and quotation mark, and ampersand, respectively.)
  *
  * References:
  * http://www.cs.princeton.edu/courses/archive/spr15/cos226/lectures/42DirectedGraphs.pdf
  *      (see WebCrawler.java for bare-bones web crawler (BFS))
  * http://docs.oracle.com/javase/7/docs/api/java/util/regex/Matcher.html
  * http://regexlib.com/CheatSheet.aspx
  * http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
  * http://examples.javacodegeeks.com/core-java/writeread-csv-files-in-java-example/
  * http://www.tedmontgomery.com/tutorial/htmlchrc.html
  **********************************************************************************/

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileWriter;
import java.io.IOException; //required for FileWriter class

public class YoutubeCrawler {
    //for CSV file delimiting
    private static final String TAB_DELIMITER = "\t";
    private static final String NEW_LINE_SEPARATOR = "\n";
    
    //change &#39; to apostrophe
    public static String toApostrophe(String word) {
        if (word.length() > 4) {
            if ((word.substring(word.length()-5, word.length())).equals("&#39;")) {
                word = word.substring(0, word.length()-5);
                word = word + "\'";
            }
        }
        return word;
    }
    //change &quot; to quotation mark
    public static String toQuotationMark(String word) {
        if (word.length() > 5) {
            if ((word.substring(word.length()-6, word.length())).equals("&quot;")) {
                word = word.substring(0, word.length()-6);
                word = word + "\"";
            }
        }
        return word;
    }
    //change &amp; to &
    public static String toAmpersand(String word) {
        if (word.length() > 4) {
            if ((word.substring(word.length()-5, word.length())).equals("&amp;")) {
                word = word.substring(0, word.length()-5);
                word = word + "&";
            }
        }
        return word;
    }
    
    //IOException required for FileWriter object
    public static void main(String[] args) throws IOException {
        Stopwatch timer = new Stopwatch(); //timer begins
        
        int counter = 0;
        
        Queue<String> queueOfURLendings = new Queue<String>();
        SET<String> foundURLs = new SET<String>(); //set of found URLs to prevent repeats
        
        FileWriter fileWriter = new FileWriter(args[2]); //filewriter for CSV file
                
        String root = args[0];
        StdOut.println("URL: " + root);
        fileWriter.append(root);
        fileWriter.append(TAB_DELIMITER);
        
        foundURLs.add(root);
        
        In in_root = new In(root);
        String input_root = in_root.readAll();

        /* ----------------------META DATA (begin)---------------------*/         
        
        //TITLE
        String title_root = "og:title\" content=\"";
        Pattern title_rootpattern = Pattern.compile(title_root);
        Matcher title_rootmatcher = title_rootpattern.matcher(input_root);
        
        while (title_rootmatcher.find()) {
            String w = title_rootmatcher.group(); //w is the matching pattern within input text (that satisfies regexp)
            String csv = "";
            for (int i = 0; i < 250; i++) {
                if (input_root.charAt(title_rootmatcher.end() + i) == ('\"') && input_root.charAt(title_rootmatcher.end() + i+1) == ('>')) break;
                csv = csv + input_root.charAt(title_rootmatcher.end() + i);
                //catch special HTML character codes: &#39; (apostrophe), &quot; (quotation mark), and &amp; (ampersand)
                csv = toApostrophe(csv);
                csv = toQuotationMark(csv);
                csv = toAmpersand(csv);
            }
            StdOut.println("Title: " + csv);
            fileWriter.append(csv);
            fileWriter.append(TAB_DELIMITER);
        }
        
        //DESCRIPTION
        String description_root = "og:description\" content=\"";
        Pattern description_rootpattern = Pattern.compile(description_root);
        Matcher description_rootmatcher = description_rootpattern.matcher(input_root);
        
        while (description_rootmatcher.find()) {
            String w = description_rootmatcher.group();
            String csv = "";
            for (int i = 0; i < 250; i++) {
                if (input_root.charAt(description_rootmatcher.end() + i) == ('\"') && input_root.charAt(description_rootmatcher.end() + i+1) == ('>')) break;
                csv = csv + input_root.charAt(description_rootmatcher.end() + i);
                //catch special HTML character codes: &#39; (apostrophe), &quot; (quotation mark), and &amp; (ampersand)
                csv = toApostrophe(csv);
                csv = toQuotationMark(csv);
                csv = toAmpersand(csv);
            }
            StdOut.println("Description: " + csv);
            fileWriter.append(csv);
            fileWriter.append(TAB_DELIMITER);
        }
        
        //TAGS
        String tags_root = "og:video:tag\" content=\"";
        Pattern tags_rootpattern = Pattern.compile(tags_root);
        Matcher tags_rootmatcher = tags_rootpattern.matcher(input_root);
        
        StdOut.print("Tags: ");
        String alltags_root = "";
        while (tags_rootmatcher.find()) {
            String w = tags_rootmatcher.group();
            String temp = "";
            for (int i = 0; i < 250; i++) {
                if (input_root.charAt(tags_rootmatcher.end() + i) == ('\"') && input_root.charAt(tags_rootmatcher.end() + i+1) == ('>')) break;
                temp = temp + input_root.charAt(tags_rootmatcher.end() + i);
                //catch special HTML character codes: &#39; (apostrophe), &quot; (quotation mark), and &amp; (ampersand)
                temp = toApostrophe(temp);
                temp = toQuotationMark(temp);
                temp = toAmpersand(temp);
            }
            alltags_root = alltags_root + temp + ", ";
        }
        if (alltags_root.length() > 2) alltags_root = alltags_root.substring(0, alltags_root.length()-2); //cut off last comma
        StdOut.println(alltags_root);
        fileWriter.append(alltags_root);
        fileWriter.append(TAB_DELIMITER);
        
        //VIEW COUNT
        String viewcount_root = "\"view_count\":\"";
        Pattern viewcount_rootpattern = Pattern.compile(viewcount_root);
        Matcher viewcount_rootmatcher = viewcount_rootpattern.matcher(input_root);
        
        StdOut.print("View Count: ");
        while (viewcount_rootmatcher.find()) {
            String w = viewcount_rootmatcher.group();
            String csv = "";
            for (int i = 0; i < 50; i++) {
                if (input_root.charAt(viewcount_rootmatcher.end() + i) == ('\"') && input_root.charAt(viewcount_rootmatcher.end() + i+1) == (',')) break;
                StdOut.print(input_root.charAt(viewcount_rootmatcher.end() + i));
                csv = csv + input_root.charAt(viewcount_rootmatcher.end() + i);
            }
            fileWriter.append(csv);
            
        }
        StdOut.println();
        
        fileWriter.append(NEW_LINE_SEPARATOR);
        
        /* ----------------------META DATA (end)---------------------*/ 
        
        String regexp_root = "/watch\\?v=((\\w){11})"; // \\? makes sure ? is the char // every youtube key has 11 chars
        Pattern pattern_root = Pattern.compile(regexp_root); // create pattern that is searched for within input text
        Matcher matcher_root = pattern_root.matcher(input_root);
        
        while (matcher_root.find()) {
            String w = matcher_root.group();
            if (!w.equals(root.substring(root.length()-20))) { //last 20 char of root is /watch?v=###########
                if (!foundURLs.contains(w)) { //add to set & queue if not in set already
                    foundURLs.add(w); //add to set
                    queueOfURLendings.enqueue(w);
                }
            }
        }

        
        
        
        
        
        
        /* ----------------------CRAWLING---------------------*/

        int n = 0;
        
        while (!queueOfURLendings.isEmpty() && counter < Integer.parseInt(args[3])-1) {
            
            String suffixURL = queueOfURLendings.dequeue();
            String fullURL = "https://www.youtube.com" + suffixURL;
            
            foundURLs.add(fullURL);
            
            In in = new In(fullURL);
            String input = in.readAll();
            
            
            //View Count
            String viewcount = "itemprop=\"interactionCount\" content=\"";
            Pattern viewcountpattern = Pattern.compile(viewcount);
            Matcher viewcountmatcher = viewcountpattern.matcher(input);

            String count = "";
            while (viewcountmatcher.find()) {
                String w = viewcountmatcher.group();
                for (int i = 0; i < 50; i++) {
                    if (input.charAt(viewcountmatcher.end() + i) == ('\"') && input.charAt(viewcountmatcher.end() + i+1) == ('>')) break;
                    count = count + input.charAt(viewcountmatcher.end() + i);
                }
            }
            
            //ensure HD
            String hd = "height\" content=\"720\""; //height" content="720"> found for all HD able vids
            Pattern hdpattern = Pattern.compile(hd);
            Matcher hdmatcher = hdpattern.matcher(input);
            
            
            //PRINT URL & METADATA IF IS HD && View Count > args[1]
            if (hdmatcher.find() && Integer.parseInt(count) > Integer.parseInt(args[1])) {
  
                counter++;
                
                StdOut.println(); //stylizing
                StdOut.println("URL: " + fullURL);
                fileWriter.append(fullURL);
                fileWriter.append(TAB_DELIMITER);
                
                /* ----------------------META DATA (begin)---------------------*/         
                
                //TITLE
                String title = "og:title\" content=\""; //title: <meta property="og:title" content=
                Pattern titlepattern = Pattern.compile(title);
                Matcher titlematcher = titlepattern.matcher(input);
                
                while (titlematcher.find()) {
                    String w = titlematcher.group();
                    String csv = "";
                    for (int i = 0; i < 250; i++) {
                        if (input.charAt(titlematcher.end() + i) == ('\"') && input.charAt(titlematcher.end() + i+1) == ('>')) break;
                        csv = csv + input.charAt(titlematcher.end() + i);
                        //catch special HTML character codes: &#39; (apostrophe), &quot; (quotation mark), and &amp; (ampersand)
                        csv = toApostrophe(csv);
                        csv = toQuotationMark(csv);
                        csv = toAmpersand(csv);
                    }
                    StdOut.println("Title: " + csv);
                    fileWriter.append(csv);
                    fileWriter.append(TAB_DELIMITER);
                }
                
                
                //DESCRIPTION
                String description = "og:description\" content=\""; //content description (abbreviated) //use eow for full
                Pattern descriptionpattern = Pattern.compile(description);
                Matcher descriptionmatcher = descriptionpattern.matcher(input);
                
                while (descriptionmatcher.find()) {
                    String w = descriptionmatcher.group();
                    String csv = "";
                    for (int i = 0; i < 250; i++) {
                        if (input.charAt(descriptionmatcher.end() + i) == ('\"') && input.charAt(descriptionmatcher.end() + i+1) == ('>')) break;
                        csv = csv + input.charAt(descriptionmatcher.end() + i);
                        //catch special HTML character codes: &#39; (apostrophe), &quot; (quotation mark), and &amp; (ampersand)
                        csv = toApostrophe(csv);
                        csv = toQuotationMark(csv);
                        csv = toAmpersand(csv);
                    }
                    StdOut.println("Description: " + csv);
                    fileWriter.append(csv);
                    fileWriter.append(TAB_DELIMITER);
                }
                
                //TAGS
                String tags = "og:video:tag\" content=\""; // tags
                Pattern tagspattern = Pattern.compile(tags);
                Matcher tagsmatcher = tagspattern.matcher(input);
                
                StdOut.print("Tags: ");
                String alltags = "";
                while (tagsmatcher.find()) {
                    String w = tagsmatcher.group();
                    String temp = "";
                    for (int i = 0; i < 250; i++) {
                        if (input.charAt(tagsmatcher.end() + i) == ('\"') && input.charAt(tagsmatcher.end() + i+1) == ('>')) break;
                        temp = temp + input.charAt(tagsmatcher.end() + i);
                        //catch special HTML character codes: &#39; (apostrophe), &quot; (quotation mark), and &amp; (ampersand)
                        temp = toApostrophe(temp);
                        temp = toQuotationMark(temp);
                        temp = toAmpersand(temp);
                    }
                    alltags = alltags + temp + ", ";
                }

                if (alltags.length() > 2) alltags = alltags.substring(0, alltags.length()-2); //cut off last comma
                StdOut.println(alltags);
                fileWriter.append(alltags);
                
                fileWriter.append(TAB_DELIMITER);
                
                StdOut.println("View Count: " + count);
                fileWriter.append(count);
                fileWriter.append(NEW_LINE_SEPARATOR);
                
                /* ----------------------META DATA (end)---------------------*/            
            }
            
            //Crawl for more vids
            String regexp = "/watch\\?v=((\\w){11})";
            Pattern pattern = Pattern.compile(regexp);
            Matcher matcher = pattern.matcher(input);
            
            while (matcher.find()) {
                String w = matcher.group();
                if (!w.equals(fullURL.substring(fullURL.length()-20))) {
                    if (!foundURLs.contains(w)) {
                        foundURLs.add(w);
                        queueOfURLendings.enqueue(w);
                    }
                }
            }

            n++;
        }
        
        //close fileWriter
        fileWriter.flush();
        fileWriter.close();
        
        StdOut.println();
        StdOut.println("Total number of HD videos entered to CSV file: " + (counter + 1)); //+1 accounts for root url
        StdOut.println("Total number of vids crawled: " + n);
        
        StdOut.println(timer.elapsedTime());
    }
}
package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.helper.DataUtil;
import org.jsoup.helper.HttpConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 核心公共接入点到jsoup功能。

 @author Jonathan Hedley */
public class Jsoup {
    private Jsoup() {}

    /**
     HTML解析成一个文档。解析器会作出明智的，平衡的文件树中的任何HTML中。

     @param html   HTML解析
     @param 基本URI所在的HTML从检索到的URL。用来解析相对URL为绝对URL，发生
     在HTML声明之前， {@code <base href>} tag.
     @return sane HTML
     */
    public static Document parse(String html, String baseUri) {
        return Parser.parse(html, baseUri);
    }

    /**
     Parse HTML into a Document, using the provided Parser. You can provide an alternate parser, such as a simple XML
     (non-HTML) parser.

     @param html    HTML to parse
     @param baseUri The URL where the HTML was retrieved from. Used to resolve relative URLs to absolute URLs, that occur
     before the HTML declares a {@code <base href>} tag.
     @param parser alternate {@link Parser#xmlParser() parser} to use.
     @return sane HTML
     */
    public static Document parse(String html, String baseUri, Parser parser) {
        return parser.parseInput(html, baseUri);
    }

    /**
     Parse HTML into a Document. As no base URI is specified, absolute URL detection relies on the HTML including a
     {@code <base href>} tag.

     @param html HTML to parse
     @return sane HTML

     @see #parse(String, String)
     */
    public static Document parse(String html) {
        return Parser.parse(html, "");
    }

    /**
     * Creates a new {@link Connection} to a URL. Use to fetch and parse a HTML page.
     * <p>
     * Use examples:
     * <ul>
     *  <li><code>Document doc = Jsoup.connect("http://example.com").userAgent("Mozilla").data("name", "jsoup").get();</code></li>
     *  <li><code>Document doc = Jsoup.connect("http://example.com").cookie("auth", "token").post();</code></li>
     * </ul>
     * @param url URL to connect to. The protocol must be {@code http} or {@code https}.
     * @return the connection. You can add data, cookies, and headers; set the user-agent, referrer, method; and then execute.
     */
    public static Connection connect(String url) {
        return HttpConnection.connect(url);
    }

    /**
     作为 H T M L 解析文件的内容。

     @param in          file to load HTML from
     @param charsetName (optional) character set of file contents. Set to {@code null} to determine from {@code http-equiv} meta tag, if
     present, or fall back to {@code UTF-8} (which is often safe to do).
     @param baseUri     The URL where the HTML was retrieved from, to resolve relative links against.
     @return sane HTML

     @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     */
    public static Document parse(File in, String charsetName, String baseUri) throws IOException {
        return DataUtil.load(in, charsetName, baseUri);
    }

    /**
     作为 HTML 解析文件的内容。文件的位置作为基 URI 用于限定的相对 Url。

     @param in          file to load HTML from
     @param charsetName (optional) character set of file contents. Set to {@code null} to determine from {@code http-equiv} meta tag, if
     present, or fall back to {@code UTF-8} (which is often safe to do).
     @return sane HTML

     @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     @see #parse(File, String, String)
     */
    public static Document parse(File in, String charsetName) throws IOException {
        return DataUtil.load(in, charsetName, in.getAbsolutePath());
    }

    /**
     读取输入的流，并将它解析到一个文档。

     @param in          input stream to read. Make sure to close it after parsing.
     @param charsetName (optional) character set of file contents. Set to {@code null} to determine from {@code http-equiv} meta tag, if
     present, or fall back to {@code UTF-8} (which is often safe to do).
     @param baseUri     The URL where the HTML was retrieved from, to resolve relative links against.
     @return sane HTML

     @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     */
    public static Document parse(InputStream in, String charsetName, String baseUri) throws IOException {
        return DataUtil.load(in, charsetName, baseUri);
    }

    /**
     读取输入的流，并将它解析到一个文档。您可以提供一个备用的解析器，如简单的 XML
     (非 HTML) 分析器。

     @param in          input stream to read. Make sure to close it after parsing.
     @param charsetName (optional) character set of file contents. Set to {@code null} to determine from {@code http-equiv} meta tag, if
     present, or fall back to {@code UTF-8} (which is often safe to do).
     @param baseUri     The URL where the HTML was retrieved from, to resolve relative links against.
     @param parser alternate {@link Parser#xmlParser() parser} to use.
     @return sane HTML

     @throws IOException if the file could not be found, or read, or if the charsetName is invalid.
     */
    public static Document parse(InputStream in, String charsetName, String baseUri, Parser parser) throws IOException {
        return DataUtil.load(in, charsetName, baseUri, parser);
    }

    /**
     解析 HTML，假设它形成 {@code 身体} 的 HTML 片段。

     @param bodyHtml body HTML fragment
     @param baseUri  URL to resolve relative URLs against.
     @return sane HTML document

     @see Document#body()
     */
    public static Document parseBodyFragment(String bodyHtml, String baseUri) {
        return Parser.parseBodyFragment(bodyHtml, baseUri);
    }

    /**
     解析 HTML，假设它形成 {@code 身体} 的 HTML 片段。

     @param bodyHtml body HTML fragment
     @return sane HTML document

     @see Document#body()
     */
    public static Document parseBodyFragment(String bodyHtml) {
        return Parser.parseBodyFragment(bodyHtml, "");
    }

    /**
     获取一个 URL，并将它解析为 HTML。提供兼容性;在大多数情况下改用 {@link #connect(String)}。<p>编码字符集由内容类型标头或 http 当量 meta 标记，或回退到 {UTF-8，@code}。
     @param url           URL to fetch (with a GET). The protocol must be {@code http} or {@code https}.
     @param timeoutMillis Connection and read timeout, in milliseconds. If exceeded, IOException is thrown.
     @return The parsed HTML.

     @throws java.net.MalformedURLException if the request URL is not a HTTP or HTTPS URL, or is otherwise malformed
     @throws HttpStatusException if the response is not OK and HTTP response errors are not ignored
     @throws UnsupportedMimeTypeException if the response mime type is not supported and those errors are not ignored
     @throws java.net.SocketTimeoutException if the connection times out
     @throws IOException if a connection or read error occurs

     @see #connect(String)
     */
    public static Document parse(URL url, int timeoutMillis) throws IOException {
        Connection con = HttpConnection.connect(url);
        con.timeout(timeoutMillis);
        return con.get();
    }

    /**
     获得安全来自不受信任输入 HTML，通过解析输入的 HTML 和过滤它通过白名单的 HTML 允许
     标记和属性。

     @param bodyHtml  input untrusted HTML (body fragment)
     @param baseUri   URL to resolve relative URLs against
     @param whitelist white-list of permitted HTML elements
     @return safe HTML (body fragment)

     @see Cleaner#clean(Document)
     */
    public static String clean(String bodyHtml, String baseUri, Whitelist whitelist) {
        Document dirty = parseBodyFragment(bodyHtml, baseUri);
        Cleaner cleaner = new Cleaner(whitelist);
        Document clean = cleaner.clean(dirty);
        return clean.body().html();
    }

    /**
     获得安全来自不受信任输入 HTML，通过解析输入的 HTML 和过滤它通过白名单的 HTML 允许
     标记和属性。

     @param bodyHtml  input untrusted HTML (body fragment)
     @param whitelist white-list of permitted HTML elements
     @return safe HTML (body fragment)

     @see Cleaner#clean(Document)
     */
    public static String clean(String bodyHtml, Whitelist whitelist) {
        return clean(bodyHtml, "", whitelist);
    }

    /**
     * 获得从不受信任的输入HTML安全的HTML，通过解析输入HTML和通过白名单过滤它允许标记和属性。
     *
     * @param bodyHtml 输入不受信任的 HTML (身体片段)
     * @param baseUri URL 解析相对 Url 反对
     * @param 白名单白名单允许的 HTML 元素
     * @param outputSettings 文档输出设置;使用控制漂亮印刷和实体逃生模式
     * @return safe HTML (body fragment)
     * @see Cleaner#clean(Document)
     */
    public static String clean(String bodyHtml, String baseUri, Whitelist whitelist, Document.OutputSettings outputSettings) {
        Document dirty = parseBodyFragment(bodyHtml, baseUri);
        Cleaner cleaner = new Cleaner(whitelist);
        Document clean = cleaner.clean(dirty);
        clean.outputSettings(outputSettings);
        return clean.body().html();
    }

    /**
     检验标签是否闭合，没有闭合true
     @param bodyHtml HTML to test
     @param whitelist whitelist to test against
     @return true 是否已被删除无标签或属性;否则为false
     @see #clean(String, org.jsoup.safety.Whitelist)
     */
    public static boolean isValid(String bodyHtml, Whitelist whitelist) {
        Document dirty = parseBodyFragment(bodyHtml, "");
        Cleaner cleaner = new Cleaner(whitelist);
        return cleaner.isValid(dirty);
    }

}

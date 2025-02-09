package com.NPU.分布式实验.实验2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Class ServerThread is the implementation of a thread of execution for the
 * multithreaded Java HTTP server.
 *
 * @author wben
 * @version 1.0
 */
class ServerThread implements Runnable {

    /**
     * String to represent the network end of line -- the carriage return line
     * feed.
     */
    static private String CRLF = "\r\n";

    /**
     * Allow a max buffers size of 8192 bytes.
     */
    static private int buffer_size = 8192;

    /**
     * Declare a thread object.
     */
    Thread t;

    /**
     * Setup the current working directory as our document root directory.
     */
    File root;

    /**
     * Each thread must have a socket associated with it.
     */
    Socket socket;

    /**
     * Direct standard output to the screen.
     */
    static PrintWriter screen = new PrintWriter(System.out, true);

    /**
     * When an instance of this class is created, the Thread t is started.
     */
    public ServerThread(Socket s, File r) {
        socket = s;
        root = r;
        t = new Thread(this, "Server Thread");
        t.start();
    }

    /**
     * Check if the requested file is at or below the document root directory.
     *
     * @param uri String string representing the requested file's uri.
     * @return boolean
     */
    private boolean isAllowed(String uri) {

        /*
         * Declare a new file object using the supplied uri.
         */
        File f = new File(root, getUriFile(uri));
        try {
            /*
             * Return whether or not the path of the request file starts with
             * the root path.
             */
            return (f.getCanonicalPath()).startsWith(root.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Builds a standard HTTP/1.0 response string.
     *
     * @param code   int int representing the HTTP code
     * @param msg    String string representing the message for the response
     * @param header String string representing the message header.
     * @return String string representing the complete response message.
     */
    private String makeResponse(int code, String msg, String header) {
        return "HTTP/1.0" + " " + code + " " + msg + CRLF + header + CRLF;
    }

    /**
     * Build the appropriate MIME string.
     *
     * @param type String string representing the MIME type.
     * @param len  int integer representing the message length
     * @return String string representing the MIME portion of the message
     */
    private String makeMIME(String type, int len) {
        String mh = "Server: SSD8Server/1.0" + CRLF + "Content-type: " + type + CRLF + "Content-length: " + len + CRLF;
        return mh;
    }

    /**
     * Generate a simple HTML document contain the necessary error codes to tell
     * the client that a problem has occured.
     *
     * @param code int integer representing the error code to send
     * @param msg  string String representing the error message
     * @return String containing the HTML document.
     */
    private String error(int code, String msg) {

        /*
         * Build the html string.
         */

        String html_page = "<HTML>" + "<BODY>" + CRLF + "<H1>" + code + " " + msg + "</H1>" + "</BODY>" + "</HTML>"
                + CRLF;

        /*
         * Add the necessary MIME type to the document.
         */
        String mh = makeMIME("text/html", html_page.length());

        /*
         * Add the appropriate response codes to the document.
         */
        String hr = makeResponse(code, msg, mh);

        /*
         * Return the entire document.
         */
        return hr + html_page;
    }

    /**
     * Get the appropriate file type by matching suffixes -- this is used to
     * build the MIME types.
     *
     * @param filename String string representing the file name
     * @return String string representing the MIME type
     */
    private String getType(String filename) {

        /*
         * If the filename ends with .html, return a "text/html" MIME type.
         */
        if (filename.endsWith(".html")) {
            return "text/html";
        } else if (filename.endsWith(".jpg")) {

            /*
             * If the filename ends with jpg, return a image/jpeg MIME type.
             */
            return "image/jpeg";

            /*
             * By default return a "text/plain" MIME type.
             */
        } else
            return "text/plain";
    }

    /**
     * Take an absolute or relative URI, and return a string which points to the
     * file relative to the server's root directory.
     *
     * @param uri String string representing the file's uri
     * @return String representing the relative path.
     */
    private String getUriFile(String uri) {
        if (uri.startsWith("http://")) {
            return uri.substring(uri.indexOf('/', 8) + 1);
        } else if (uri.startsWith("/")) {
            return uri.substring(1);
        } else
            return uri;
    }

    /**
     * handles a client's GET request
     *
     * @param ostream BufferedOutputStream stream of data written to the socket
     * @param uri     String string representing the uri
     */
    private void handleGet(BufferedOutputStream ostream, String uri) {
        try {
            /**
             * Build the filename and try to open the file. If an error occurs
             * (if the file cannot be found), return an HTTP 404 error.
             */
            String filename = getUriFile(uri);
            File f = new File(root, filename);
            if (!f.isFile()) {
                ostream.write((error(404, "File not found")).getBytes());
            } else {
                /*
                 * Else, get the data file the file and send it with the
                 * appropriate response headers for HTTP and MIME.
                 *
                 * First, set the header and MIME type.
                 */
                String type = getType(f.getName());
                String header = makeMIME(type, (int) f.length());
                String response = makeResponse(200, "OK", header);
                ostream.write(response.getBytes());

                /*
                 * Now open the data file, read it, and send it back to the
                 * client.
                 */
                FileInputStream fstream = new FileInputStream(f);
                int bufsize = 0;

                /*
                 * Must use a byte array to handle images.
                 */
                byte[] buffer = new byte[buffer_size];

                /*
                 * Loop while there is data to be read.
                 */
                while ((bufsize = fstream.read(buffer)) != -1) {
                    ostream.write(buffer);
                }
            }
            /*
             * If an exception occurs, print out a stack trace.
             */
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Handle an HTTP PUT request.
     *
     * @param istream BufferedInputStream input stream from socket
     * @param ostream BufferedOutputStream output stream to socket
     * @param uri     String string representing the file's uri
     * @param len     int length of input
     */
    private void handlePut(BufferedInputStream istream, BufferedOutputStream ostream, String uri, int len) {
        try {
            /*
             * See if the file already exists in the root directory. If so, then
             * simply return an OK (200) message. If you are not allowed to
             * upload a file to this directory return a forbidden (403) message.
             */

            String filename = getUriFile(uri);
            File f = new File(root, filename);
            String reply;
            if (f.exists()) {
                if (!f.isFile()) {
                    ostream.write((error(403, "Forbidden")).getBytes());
                    return;
                } else {
                    reply = error(200, "OK");
                }
            } else {
                /*
                 * If the file is uploadable and it is not already on the
                 * server, then cbuild a 201 "created" message.
                 */
                f.getParentFile().mkdirs();
                reply = error(201, "Created");
            }

            /*
             * Create a file on the local disk and write the input bytes from
             * the socket to it.
             */
            FileOutputStream fstream = new FileOutputStream(f);
            byte[] buffer = new byte[buffer_size];
            int count = 0;
            while (count < len && ((count += istream.read(buffer)) != -1)) {
                fstream.write(buffer);
            }

            /*
             * Be sure to flush the output stream.
             */
            fstream.flush();
            fstream.close();
            ostream.write(reply.getBytes());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * run method serves one connection, then closes.
     */
    public void run() {

        /*
         * Buffer to hold the input data from the socket.
         */
        byte[] buffer = new byte[buffer_size];

        /*
         * Boolean to tell the while loop to continue running.
         */
        boolean inRequest = true;

        /*
         * Integers storing the buffer size (of the incoming message), and the
         * incoming character -- c.
         */
        int c, bufsize = 0;
        try {

            /*
             * Input and output streams for the sockets.
             */
            BufferedOutputStream ostream = new BufferedOutputStream(socket.getOutputStream());
            BufferedInputStream istream = new BufferedInputStream(socket.getInputStream());

            /*
             * Continue reading until two network end of line ("\r\n\r\n")
             * characters are read.
             */
            while (inRequest && ((c = istream.read()) != -1) && bufsize < buffer_size) {
                switch (c) {
                    case '\r':
                        break;
                    case '\n':
                        if (bufsize > 0 && buffer[bufsize - 1] == (byte) c) {
                            inRequest = false;
                            break;
                        }
                    default:
                        buffer[bufsize++] = (byte) c;
                }
            }

            /*
             * Parse the request line using a StringTokenizer object.
             */
            StringTokenizer st = new StringTokenizer(new String(buffer));
            try {

                /*
                 * Method is the first token, uri is the second token, and
                 * version is the third token.
                 */
                String method = st.nextToken();
                String uri = st.nextToken();
                String version = st.nextToken();

                /*
                 * Handle a GET HTTP request. Return a forbidden (403) message
                 * if you cannot access the URI.
                 */
                if (method.equals("GET")) {
                    if (isAllowed(uri)) {
                        handleGet(ostream, uri);
                    } else {
                        ostream.write((error(403, "Forbidden")).getBytes());
                    }
                    /*
                     * Handle a PUT method.
                     */
                } else if (method.equals("PUT")) {
                    /*
                     * We must determine the content length for a PUT request.
                     */
                    int len = -1;
                    StringTokenizer mreader = new StringTokenizer(new String(buffer), "\r\n");
                    while (mreader.hasMoreTokens()) {
                        String line = mreader.nextToken();
                        if (line.startsWith("Content-Length:")) {
                            String length = line.substring(line.indexOf(' ') + 1);
                            len = new Integer(length).intValue();
                            break;
                        }
                    }
                    /*
                     * If the length is greater than zero, attempt to process
                     * the PUT request.
                     */
                    if (len > 0) {
                        handlePut(istream, ostream, uri, len);
                    } else {
                        /*
                         * If an error occurs, send a Bad Request (400) message
                         * back to the client.
                         */
                        ostream.write((error(400, "Bad Request.")).getBytes());
                    }
                } else {
                    /*
                     * If the request was not a GET or a PUT request, then send
                     * a NOT IMPLEMENTED (501) code.
                     */
                    ostream.write((error(501, "Not Implemented")).getBytes());
                }
            } catch (NoSuchElementException nse) {
                screen.println("Parse failed.");
                nse.printStackTrace();
            }
            /*
             * Be sure to flush out the stream and close off the socket when we
             * are complete.
             */
            ostream.flush();
            socket.close();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }
}

/**
 * Threaded server class can server multiple concurrent HTTP requests.
 */
public class ThreadedServer {

    /**
     * Set the standard output to the screen.
     */
    static PrintWriter screen = new PrintWriter(System.out, true);

    /**
     * Declare the document root directory.
     */
    static File root;

    /**
     * Server's main method. Two input parameters must be supplied - a document
     * root (mandatory) and a listening port (optional).
     */
    public static void main(String[] args) {

        /*
         * Main method needs a server socket to listen on and a regular socket
         * to communicate with.
         */
        ServerSocket serverSocket = null;
        Socket socket = null;

        /*
         * The server must listen on a port.
         */
        int port;

        /*
         * If incorrect input arguments are supplied, print usage instructions.
         */
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: Server </path/to/www/directory> [port]");
            return;
        }

        /*
         * Parse the input argument for the port if it was supplied, otherwise
         * default to port 8000.
         */
        if (args.length == 2) {
            port = new Integer(args[1]).intValue();
        } else {
            port = 80;
        }

        /*
         * Check to see if the root document directory exists, and and that it
         * is actually a directory.
         */
        try {
            root = new File(args[0]);
            if (!root.isDirectory()) {
                System.err.println(root.getAbsolutePath() + " does not exist or is not a directory");
                return;
            }
        } catch (SecurityException se) {
            System.err.println("Security exception: " + se);
            se.printStackTrace();
            return;
        }

        /*
         * Create the server socket to listen for incoming connections.
         */
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            System.err.println("Exception creating listening socket");
        }

        /*
         * Loop forever accepting incoming connections. Create a new thread of
         * execution for each request.
         */
        while (true) {
            try {

                /*
                 * Accept an incoming connection. This will block until a client
                 * establishes a connection. The function returns a reference to
                 * a regular, non-server socket.
                 */
                socket = serverSocket.accept();

                /*
                 * Create a new server thread for each request.
                 */
                new ServerThread(socket, root);
            } catch (Exception e) {
                screen.println(e);
                e.printStackTrace();
            }
        }
    }
}

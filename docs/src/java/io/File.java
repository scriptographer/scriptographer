package java.io;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Random;
import java.security.AccessController;
import java.security.AccessControlException;
import sun.security.action.GetPropertyAction;


/**
 * An abstract representation of file and directory pathnames.
 * 
 * <p>
 * User interfaces and operating systems use system-dependent <em>pathname
 * strings</em>
 * to name files and directories. This class presents an abstract,
 * system-independent view of hierarchical pathnames. An
 * <em>abstract pathname</em> has two components:
 * 
 * <ol>
 * <li> An optional system-dependent <em>prefix</em> string, such as a
 * disk-drive specifier, <code>"/"</code>&nbsp;for the UNIX root directory,
 * or <code>"\\\\"</code>&nbsp;for a Microsoft Windows UNC pathname, and
 * <li> A sequence of zero or more string <em>names</em>.
 * </ol>
 * 
 * Each name in an abstract pathname except for the last denotes a directory;
 * the last name may denote either a directory or a file. The <em>empty</em>
 * abstract pathname has no prefix and an empty name sequence.
 * 
 * <p>
 * The conversion of a pathname string to or from an abstract pathname is
 * inherently system-dependent. When an abstract pathname is converted into a
 * pathname string, each name is separated from the next by a single copy of the
 * default <em>separator character</em>. The default name-separator character
 * is defined by the system property <code>file.separator</code>, and is made
 * available in the public static fields <code>{@link
 * #separator}</code> and
 * <code>{@link #separatorChar}</code> of this class. When a pathname string
 * is converted into an abstract pathname, the names within it may be separated
 * by the default name-separator character or by any other name-separator
 * character that is supported by the underlying system.
 * 
 * <p>
 * A pathname, whether abstract or in string form, may be either
 * <em>absolute</em> or <em>relative</em>. An absolute pathname is complete
 * in that no other information is required in order to locate the file that it
 * denotes. A relative pathname, in contrast, must be interpreted in terms of
 * information taken from some other pathname. By default the classes in the
 * <code>java.io</code> package always resolve relative pathnames against the
 * current user directory. This directory is named by the system property
 * <code>user.dir</code>, and is typically the directory in which the Java
 * virtual machine was invoked.
 * 
 * <p>
 * The prefix concept is used to handle root directories on UNIX platforms, and
 * drive specifiers, root directories and UNC pathnames on Microsoft Windows
 * platforms, as follows:
 * 
 * <ul>
 * 
 * <li> For UNIX platforms, the prefix of an absolute pathname is always
 * <code>"/"</code>. Relative pathnames have no prefix. The abstract pathname
 * denoting the root directory has the prefix <code>"/"</code> and an empty
 * name sequence.
 * 
 * <li> For Microsoft Windows platforms, the prefix of a pathname that contains
 * a drive specifier consists of the drive letter followed by <code>":"</code>
 * and possibly followed by <code>"\\"</code> if the pathname is absolute. The
 * prefix of a UNC pathname is <code>"\\\\"</code>; the hostname and the
 * share name are the first two names in the name sequence. A relative pathname
 * that does not specify a drive has no prefix.
 * 
 * </ul>
 * 
 * <p>
 * Instances of the <code>File</code> class are immutable; that is, once
 * created, the abstract pathname represented by a <code>File</code> object
 * will never change.
 */

public class File {

    /**
	 * The system-dependent default name-separator character, represented as a
	 * string for convenience. This string contains a single character, namely
	 * <code>{@link #separatorChar}</code>.
	 */
    public static final String separator = "" + separatorChar;

    /**
	 * The system-dependent path-separator character, represented as a string
	 * for convenience. This string contains a single character, namely
	 * <code>{@link #pathSeparatorChar}</code>.
	 */
    public static final String pathSeparator = "" + pathSeparatorChar;

    /**
	 * Creates a new <code>File</code> instance by converting the given
	 * pathname string into an abstract pathname. If the given string is the
	 * empty string, then the result is the empty abstract pathname.
	 * 
	 * @param pathname A pathname string
	 * @throws NullPointerException If the <code>pathname</code> argument is
	 *         <code>null</code>
	 */
    public File(String pathname) {
    }

     /**
	 * Creates a new <code>File</code> instance from a parent pathname string
	 * and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>File</code> instance is created as if by invoking the
	 * single-argument <code>File</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> pathname string is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to
	 * denote either a directory or a file. If the <code>child</code> pathname
	 * string is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty string then
	 * the new <code>File</code> instance is created by converting
	 * <code>child</code> into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 * 
	 * @param parent The parent pathname string
	 * @param child The child pathname string
	 * @throws NullPointerException If <code>child</code> is <code>null</code>
	 */
    public File(String parent, String child) {
    }

    /**
	 * Creates a new <code>File</code> instance from a parent abstract
	 * pathname and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>File</code> instance is created as if by invoking the
	 * single-argument <code>File</code> constructor on the given
	 * <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> abstract pathname is taken to denote
	 * a directory, and the <code>child</code> pathname string is taken to
	 * denote either a directory or a file. If the <code>child</code> pathname
	 * string is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty abstract
	 * pathname then the new <code>File</code> instance is created by
	 * converting <code>child</code> into an abstract pathname and resolving
	 * the result against a system-dependent default directory. Otherwise each
	 * pathname string is converted into an abstract pathname and the child
	 * abstract pathname is resolved against the parent.
	 * 
	 * @param parent The parent abstract pathname
	 * @param child The child pathname string
	 * @throws NullPointerException If <code>child</code> is <code>null</code>
	 */
    public File(File parent, String child) {
    }

    /**
	 * Creates a new <tt>File</tt> instance by converting the given
	 * <tt>file:</tt> URI into an abstract pathname.
	 * 
	 * <p>
	 * The exact form of a <tt>file:</tt> URI is system-dependent, hence the
	 * transformation performed by this constructor is also system-dependent.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i> it is guaranteed that
	 * 
	 * <blockquote><tt>
     * new File(</tt><i>&nbsp;f</i><tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i><tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
     * </tt></blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. This relationship typically does not hold, however,
	 * when a <tt>file:</tt> URI that is created in a virtual machine on one
	 * operating system is converted into an abstract pathname in a virtual
	 * machine on a different operating system.
	 * 
	 * @param uri An absolute, hierarchical URI with a scheme equal to
	 *        <tt>"file"</tt>, a non-empty path component, and undefined
	 *        authority, query, and fragment components
	 * 
	 * @throws NullPointerException If <tt>uri</tt> is <tt>null</tt>
	 * 
	 * @throws IllegalArgumentException If the preconditions on the parameter do
	 *         not hold
	 * 
	 * @see #toURI()
	 * @see java.net.URI
	 * @since 1.4
	 */
    public File(URI uri) {
    }


    /* -- Path-component accessors -- */

    /**
	 * Returns the name of the file or directory denoted by this abstract
	 * pathname. This is just the last name in the pathname's name sequence. If
	 * the pathname's name sequence is empty, then the empty string is returned.
	 * 
	 * @return The name of the file or directory denoted by this abstract
	 *         pathname, or the empty string if this pathname's name sequence is
	 *         empty
	 */
    public String getName() {
    }

    /**
	 * Returns the pathname string of this abstract pathname's parent, or
	 * <code>null</code> if this pathname does not name a parent directory.
	 * 
	 * <p>
	 * The <em>parent</em> of an abstract pathname consists of the pathname's
	 * prefix, if any, and each name in the pathname's name sequence except for
	 * the last. If the name sequence is empty then the pathname does not name a
	 * parent directory.
	 * 
	 * @return The pathname string of the parent directory named by this
	 *         abstract pathname, or <code>null</code> if this pathname does
	 *         not name a parent
	 */
    public String getParent() {
    }

    /**
	 * Returns the abstract pathname of this abstract pathname's parent, or
	 * <code>null</code> if this pathname does not name a parent directory.
	 * 
	 * <p>
	 * The <em>parent</em> of an abstract pathname consists of the pathname's
	 * prefix, if any, and each name in the pathname's name sequence except for
	 * the last. If the name sequence is empty then the pathname does not name a
	 * parent directory.
	 * 
	 * @return The abstract pathname of the parent directory named by this
	 *         abstract pathname, or <code>null</code> if this pathname does
	 *         not name a parent
	 * 
	 * @since 1.2
	 */
    public File getParentFile() {
    }

    /**
	 * Converts this abstract pathname into a pathname string. The resulting
	 * string uses the {@link #separator default name-separator character} to
	 * separate the names in the name sequence.
	 * 
	 * @return The string form of this abstract pathname
	 */
    public String getPath() {
    }


    /* -- Path operations -- */

    /**
	 * Tests whether this abstract pathname is absolute. The definition of
	 * absolute pathname is system dependent. On UNIX systems, a pathname is
	 * absolute if its prefix is <code>"/"</code>. On Microsoft Windows
	 * systems, a pathname is absolute if its prefix is a drive specifier
	 * followed by <code>"\\"</code>, or if its prefix is <code>"\\\\"</code>.
	 * 
	 * @return {@true if this abstract pathname is absolute}
	 */
    public boolean isAbsolute() {
    }

    /**
	 * Returns the absolute pathname string of this abstract pathname.
	 * 
	 * <p>
	 * If this abstract pathname is already absolute, then the pathname string
	 * is simply returned as if by the <code>{@link #getPath}</code> method.
	 * If this abstract pathname is the empty abstract pathname then the
	 * pathname string of the current user directory, which is named by the
	 * system property <code>user.dir</code>, is returned. Otherwise this
	 * pathname is resolved in a system-dependent way. On UNIX systems, a
	 * relative pathname is made absolute by resolving it against the current
	 * user directory. On Microsoft Windows systems, a relative pathname is made
	 * absolute by resolving it against the current directory of the drive named
	 * by the pathname, if any; if not, it is resolved against the current user
	 * directory.
	 * 
	 * @return The absolute pathname string denoting the same file or directory
	 *         as this abstract pathname
	 * 
	 * @throws SecurityException If a required system property value cannot be
	 *         accessed.
	 * 
	 * @see java.io.File#isAbsolute()
	 */
    public String getAbsolutePath() {
    }

    /**
	 * Returns the absolute form of this abstract pathname. Equivalent to
	 * <code>new&nbsp;File(this.{@link #getAbsolutePath}())</code>.
	 * 
	 * @return The absolute abstract pathname denoting the same file or
	 *         directory as this abstract pathname
	 * 
	 * @throws SecurityException If a required system property value cannot be
	 *         accessed.
	 * 
	 * @since 1.2
	 */
    public File getAbsoluteFile() {
    }

    /**
	 * Returns the canonical pathname string of this abstract pathname.
	 * 
	 * <p>
	 * A canonical pathname is both absolute and unique. The precise definition
	 * of canonical form is system-dependent. This method first converts this
	 * pathname to absolute form if necessary, as if by invoking the
	 * {@link #getAbsolutePath} method, and then maps it to its unique form in a
	 * system-dependent way. This typically involves removing redundant names
	 * such as <tt>"."</tt> and <tt>".."</tt> from the pathname, resolving
	 * symbolic links (on UNIX platforms), and converting drive letters to a
	 * standard case (on Microsoft Windows platforms).
	 * 
	 * <p>
	 * Every pathname that denotes an existing file or directory has a unique
	 * canonical form. Every pathname that denotes a nonexistent file or
	 * directory also has a unique canonical form. The canonical form of the
	 * pathname of a nonexistent file or directory may be different from the
	 * canonical form of the same pathname after the file or directory is
	 * created. Similarly, the canonical form of the pathname of an existing
	 * file or directory may be different from the canonical form of the same
	 * pathname after the file or directory is deleted.
	 * 
	 * @return The canonical pathname string denoting the same file or directory
	 *         as this abstract pathname
	 * 
	 * @throws IOException If an I/O error occurs, which is possible because the
	 *         construction of the canonical pathname may require filesystem
	 *         queries
	 * 
	 * @throws SecurityException If a required system property value cannot be
	 *         accessed, or if a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkRead}</code>
	 *         method denies read access to the file
	 * 
	 * @since JDK1.1
	 */
    public String getCanonicalPath() throws IOException {
    }

    /**
	 * Returns the canonical form of this abstract pathname. Equivalent to
	 * <code>new&nbsp;File(this.{@link #getCanonicalPath}())</code>.
	 * 
	 * @return The canonical pathname string denoting the same file or directory
	 *         as this abstract pathname
	 * 
	 * @throws IOException If an I/O error occurs, which is possible because the
	 *         construction of the canonical pathname may require filesystem
	 *         queries
	 * 
	 * @throws SecurityException If a required system property value cannot be
	 *         accessed, or if a security manager exists and its <code>{@link
     *          java.lang.SecurityManager#checkRead}</code>
	 *         method denies read access to the file
	 * 
	 * @since 1.2
	 */
    public File getCanonicalFile() throws IOException {
    }

    /**
	 * Converts this abstract pathname into a <code>file:</code> URL. The
	 * exact form of the URL is system-dependent. If it can be determined that
	 * the file denoted by this abstract pathname is a directory, then the
	 * resulting URL will end with a slash.
	 * 
	 * <p>
	 * <b>Usage note:</b> This method does not automatically escape characters
	 * that are illegal in URLs. It is recommended that new code convert an
	 * abstract pathname into a URL by first converting it into a URI, via the
	 * {@link #toURI() toURI} method, and then converting the URI into a URL via
	 * the {@link java.net.URI#toURL() URI.toURL} method.
	 * 
	 * @return A URL object representing the equivalent file URL
	 * 
	 * @throws MalformedURLException If the path cannot be parsed as a URL
	 * 
	 * @see #toURI()
	 * @see java.net.URI
	 * @see java.net.URI#toURL()
	 * @see java.net.URL
	 * @since 1.2
	 */
    public URL toURL() throws MalformedURLException {
    }

    /**
	 * Constructs a <tt>file:</tt> URI that represents this abstract pathname.
	 * 
	 * <p>
	 * The exact form of the URI is system-dependent. If it can be determined
	 * that the file denoted by this abstract pathname is a directory, then the
	 * resulting URI will end with a slash.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i>, it is guaranteed that
	 * 
	 * <blockquote><tt>
     * new {@link #File(java.net.URI) File}(</tt><i>&nbsp;f</i><tt>.toURI()).equals(</tt><i>&nbsp;f</i><tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
     * </tt></blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. Due to the system-dependent nature of abstract
	 * pathnames, however, this relationship typically does not hold when a
	 * <tt>file:</tt> URI that is created in a virtual machine on one
	 * operating system is converted into an abstract pathname in a virtual
	 * machine on a different operating system.
	 * 
	 * @return An absolute, hierarchical URI with a scheme equal to
	 *         <tt>"file"</tt>, a path representing this abstract pathname,
	 *         and undefined authority, query, and fragment components
	 * 
	 * @see #File(java.net.URI)
	 * @see java.net.URI
	 * @see java.net.URI#toURL()
	 * @since 1.4
	 */
    public URI toURI() {
    }


    /* -- Attribute accessors -- */

    /**
	 * Tests whether the application can read the file denoted by this abstract
	 * pathname.
	 * 
	 * @return {@true if and only if the file specified by this
	 *         abstract pathname exists <em>and</em> can be read by the
	 *         application}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the file
	 */
    public boolean canRead() {
    }

    /**
	 * Tests whether the application can modify the file denoted by this
	 * abstract pathname.
	 * 
	 * @return {@true if and only if the file system actually
	 *         contains a file denoted by this abstract pathname <em>and</em>
	 *         the application is allowed to write to the file}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *         method denies write access to the file
	 */
    public boolean canWrite() {
    }

    /**
	 * Tests whether the file or directory denoted by this abstract pathname
	 * exists.
	 * 
	 * @return {@true if and only if the file or directory denoted
	 *         by this abstract pathname exists}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the file or directory
	 */
    public boolean exists() {
    }

    /**
	 * Tests whether the file denoted by this abstract pathname is a directory.
	 * 
	 * @return {@true if and only if the file denoted by this
	 *         abstract pathname exists <em>and</em> is a directory}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the file
	 */
    public boolean isDirectory() {
    }

    /**
	 * Tests whether the file denoted by this abstract pathname is a normal
	 * file. A file is <em>normal</em> if it is not a directory and, in
	 * addition, satisfies other system-dependent criteria. Any non-directory
	 * file created by a Java application is guaranteed to be a normal file.
	 * 
	 * @return {@true if and only if the file denoted by this
	 *         abstract pathname exists <em>and</em> is a normal file}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the file
	 */
    public boolean isFile() {
    }

    /**
	 * Tests whether the file named by this abstract pathname is a hidden file.
	 * The exact definition of <em>hidden</em> is system-dependent. On UNIX
	 * systems, a file is considered to be hidden if its name begins with a
	 * period character (<code>'.'</code>). On Microsoft Windows systems, a
	 * file is considered to be hidden if it has been marked as such in the
	 * filesystem.
	 * 
	 * @return {@true if and only if the file denoted by this
	 *         abstract pathname is hidden according to the conventions of the
	 *         underlying platform}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the file
	 * 
	 * @since 1.2
	 */
    public boolean isHidden() {
    }

    /**
	 * Returns the time that the file denoted by this abstract pathname was last
	 * modified.
	 * 
	 * @return A <code>long</code> value representing the time the file was
	 *         last modified, measured in milliseconds since the epoch (00:00:00
	 *         GMT, January 1, 1970), or <code>0L</code> if the file does not
	 *         exist or if an I/O error occurs
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the file
	 */
    public long lastModified() {
    }

     /**
	 * Deletes the file or directory denoted by this abstract pathname. If this
	 * pathname denotes a directory, then the directory must be empty in order
	 * to be deleted.
	 * 
	 * @return {@true if and only if the file or directory is
	 *         successfully deleted}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkDelete}</code>
	 *         method denies delete access to the file
	 */
    public boolean remove() {
    }

    /**
	 * Returns an array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname.
	 * 
	 * <p>
	 * If this abstract pathname does not denote a directory, then this method
	 * returns <code>null</code>. Otherwise an array of strings is returned,
	 * one for each file or directory in the directory. Names denoting the
	 * directory itself and the directory's parent directory are not included in
	 * the result. Each string is a file name rather than a complete path.
	 * 
	 * <p>
	 * There is no guarantee that the name strings in the resulting array will
	 * appear in any specific order; they are not, in particular, guaranteed to
	 * appear in alphabetical order.
	 * 
	 * @return An array of strings naming the files and directories in the
	 *         directory denoted by this abstract pathname. The array will be
	 *         empty if the directory is empty. Returns <code>null</code> if
	 *         this abstract pathname does not denote a directory, or if an I/O
	 *         error occurs.
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the directory
	 */
    public String[] list() {
    }

    /**
	 * Returns an array of strings naming the files and directories in the
	 * directory denoted by this abstract pathname that satisfy the specified
	 * filter. The behavior of this method is the same as that of the
	 * <code>{@link #list()}</code> method, except that the strings in the
	 * returned array must satisfy the filter. If the given <code>filter</code>
	 * is <code>null</code> then all names are accepted. Otherwise, a name
	 * satisfies the filter if and only if the value <code>true</code> results
	 * when the <code>{@link
     * FilenameFilter#accept}</code> method of the
	 * filter is invoked on this abstract pathname and the name of a file or
	 * directory in the directory that it denotes.
	 * 
	 * @param filter A filename filter
	 * 
	 * @return An array of strings naming the files and directories in the
	 *         directory denoted by this abstract pathname that were accepted by
	 *         the given <code>filter</code>. The array will be empty if the
	 *         directory is empty or if no names were accepted by the filter.
	 *         Returns <code>null</code> if this abstract pathname does not
	 *         denote a directory, or if an I/O error occurs.
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the directory
	 */
    public String[] list(FilenameFilter filter) {
    }

    /**
	 * Returns an array of abstract pathnames denoting the files in the
	 * directory denoted by this abstract pathname.
	 * 
	 * <p>
	 * If this abstract pathname does not denote a directory, then this method
	 * returns <code>null</code>. Otherwise an array of <code>File</code>
	 * objects is returned, one for each file or directory in the directory.
	 * Pathnames denoting the directory itself and the directory's parent
	 * directory are not included in the result. Each resulting abstract
	 * pathname is constructed from this abstract pathname using the
	 * <code>{@link #File(java.io.File, java.lang.String)
     * File(File,&nbsp;String)}</code>
	 * constructor. Therefore if this pathname is absolute then each resulting
	 * pathname is absolute; if this pathname is relative then each resulting
	 * pathname will be relative to the same directory.
	 * 
	 * <p>
	 * There is no guarantee that the name strings in the resulting array will
	 * appear in any specific order; they are not, in particular, guaranteed to
	 * appear in alphabetical order.
	 * 
	 * @return An array of abstract pathnames denoting the files and directories
	 *         in the directory denoted by this abstract pathname. The array
	 *         will be empty if the directory is empty. Returns
	 *         <code>null</code> if this abstract pathname does not denote a
	 *         directory, or if an I/O error occurs.
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the directory
	 * 
	 * @since 1.2
	 */
    public File[] listFiles() {
    }

    /**
	 * Returns an array of abstract pathnames denoting the files and directories
	 * in the directory denoted by this abstract pathname that satisfy the
	 * specified filter. The behavior of this method is the same as that of the
	 * <code>{@link #listFiles()}</code> method, except that the pathnames in
	 * the returned array must satisfy the filter. If the given
	 * <code>filter</code> is <code>null</code> then all pathnames are
	 * accepted. Otherwise, a pathname satisfies the filter if and only if the
	 * value <code>results</code> when the
	 * <code>{@link FilenameFilter#accept}</code> method of the filter is
	 * invoked on this abstract pathname and the name of a file or directory in
	 * the directory that it denotes.
	 * 
	 * @param filter A filename filter
	 * 
	 * @return An array of abstract pathnames denoting the files and directories
	 *         in the directory denoted by this abstract pathname. The array
	 *         will be empty if the directory is empty. Returns
	 *         <code>null</code> if this abstract pathname does not denote a
	 *         directory, or if an I/O error occurs.
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the directory
	 * 
	 * @since 1.2
	 */
    public File[] listFiles(FilenameFilter filter) {
    }

    /**
	 * Returns an array of abstract pathnames denoting the files and directories
	 * in the directory denoted by this abstract pathname that satisfy the
	 * specified filter. The behavior of this method is the same as that of the
	 * <code>{@link #listFiles()}</code> method, except that the pathnames in
	 * the returned array must satisfy the filter. If the given
	 * <code>filter</code> is <code>null</code> then all pathnames are
	 * accepted. Otherwise, a pathname satisfies the filter if and only if the
	 * value <code>results</code> when the
	 * <code>{@link FileFilter#accept(java.io.File)}</code> method of the
	 * filter is invoked on the pathname.
	 * 
	 * @param filter A file filter
	 * 
	 * @return An array of abstract pathnames denoting the files and directories
	 *         in the directory denoted by this abstract pathname. The array
	 *         will be empty if the directory is empty. Returns
	 *         <code>null</code> if this abstract pathname does not denote a
	 *         directory, or if an I/O error occurs.
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method denies read access to the directory
	 * 
	 * @since 1.2
	 */
    public File[] listFiles(FileFilter filter) {
    }

    /**
	 * Creates the directory named by this abstract pathname.
	 * 
	 * @return {@true if and only if the directory was created}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *         method does not permit the named directory to be created
	 */
    public boolean mkdir() {
    }

    /**
	 * Creates the directory named by this abstract pathname, including any
	 * necessary but nonexistent parent directories. Note that if this operation
	 * fails it may have succeeded in creating some of the necessary parent
	 * directories.
	 * 
	 * @return {@true if and only if the directory was created,
	 *         along with all necessary parent directories}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}</code>
	 *         method does not permit verification of the existence of the named
	 *         directory and all necessary parent directories; or if the
	 *         <code>{@link 
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *         method does not permit the named directory and all necessary
	 *         parent directories to be created
	 */
    public boolean mkdirs() {
    }

    /**
	 * Renames the file denoted by this abstract pathname.
	 * 
	 * <p>
	 * Many aspects of the behavior of this method are inherently
	 * platform-dependent: The rename operation might not be able to move a file
	 * from one filesystem to another, it might not be atomic, and it might not
	 * succeed if a file with the destination abstract pathname already exists.
	 * The return value should always be checked to make sure that the rename
	 * operation was successful.
	 * 
	 * @param dest The new abstract pathname for the named file
	 * 
	 * @return {@true if and only if the renaming succeeded}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *         method denies write access to either the old or new pathnames
	 * 
	 * @throws NullPointerException If parameter <code>dest</code> is
	 *         <code>null</code>
	 */
    public boolean renameTo(File dest) {
    }

    /**
	 * Sets the last-modified time of the file or directory named by this
	 * abstract pathname.
	 * 
	 * <p>
	 * All platforms support file-modification times to the nearest second, but
	 * some provide more precision. The argument will be truncated to fit the
	 * supported precision. If the operation succeeds and no intervening
	 * operations on the file take place, then the next invocation of the
	 * <code>{@link #lastModified}</code> method will return the (possibly
	 * truncated) <code>time</code> argument that was passed to this method.
	 * 
	 * @param time The new last-modified time, measured in milliseconds since
	 *        the epoch (00:00:00 GMT, January 1, 1970)
	 * 
	 * @return {@true if and only if the operation succeeded}
	 * 
	 * @throws IllegalArgumentException If the argument is negative
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *         method denies write access to the named file
	 * 
	 * @since 1.2
	 */
    public boolean setLastModified(long time) {
    }

    /**
	 * Marks the file or directory named by this abstract pathname so that only
	 * read operations are allowed. After invoking this method the file or
	 * directory is guaranteed not to change until it is either deleted or
	 * marked to allow write access. Whether or not a read-only file or
	 * directory may be deleted depends upon the underlying system.
	 * 
	 * @return {@true if and only if the operation succeeded}
	 * 
	 * @throws SecurityException If a security manager exists and its
	 *         <code>{@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}</code>
	 *         method denies write access to the named file
	 * 
	 * @since 1.2
	 */
    public boolean setReadOnly() {
    }
}

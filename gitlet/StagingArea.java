package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** Class represents the staging area of the control system.
 * @author Duc Nguyen
 */
public class StagingArea implements Serializable {

    /** List of old files to be removed. */
    private ArrayList<String> _toBeRemoved = new ArrayList<>();

    /** A mapping of new files to their SHA-1. */
    private HashMap<String, String> _newFiles = new HashMap<>();

    /** Stage a file for removal.
     * @param fileName the file name. */
    public void stageToBeRemoved(String fileName) {
        _toBeRemoved.add(fileName);
    }

    /** Stage new files.
     * @param fileName the file name.
     * @param fileSHA1 the file hash value. */
    public void stage(String fileName, String fileSHA1) {
        _newFiles.put(fileName, fileSHA1);
    }

    /** Check if a file is staged for addition.
     * @param fileName the file name.
     * @return boolean. */
    public boolean checkStaged(String fileName) {
        if (!_newFiles.isEmpty()) {
            return _newFiles.containsKey(fileName);
        }
        return false;
    }

    /** Check if a file is staged for removal.
     * @param fileName the file name.
     * @return boolean. */
    public boolean checkRemoved(String fileName) {
        if (!_toBeRemoved.isEmpty()) {
            return _toBeRemoved.contains(fileName);
        }
        return false;
    }

    /** Unstage a file from the corresponding map.
     * @param filename the file name. */
    public void unstage(String filename) {
        if (!_newFiles.isEmpty()) {
            _newFiles.remove(filename);
        }
    }

    /** Unremove a file from the corresponding map.
     * @param filename the file name. */
    public void unremove(String filename) {
        if (!_toBeRemoved.isEmpty()) {
            _toBeRemoved.remove(filename);
        }
    }

    /** Clear the current staging area. */
    public void clear() {
        _toBeRemoved.clear();
        _newFiles.clear();
    }

    /** Check to see if the current staging area is empty.
     * @return boolean. */
    public boolean isEmpty() {
        return _toBeRemoved.isEmpty() && _newFiles.isEmpty();
    }

    /** Return the list of old files to be removed.
     * @return ArrayList of String */
    public ArrayList<String> getToBeRemoved() {
        return _toBeRemoved;
    }

    /** Return the mapping of new files to their SHA-1's.
     * @return a HashMap of String-String. */
    public HashMap<String, String> getFilesSHA1Map() {
        return _newFiles;
    }

    /** Get the SHA-1 of a file staged.
     * @param fileName the file name.
     * @return String. */
    public String getSHA1(String fileName) {
        if (!_newFiles.isEmpty() && _newFiles.containsKey(fileName)) {
            return _newFiles.get(fileName);
        }
        return "";
    }
}

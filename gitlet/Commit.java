package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;

/** Class that represents each commit of the control system.
 * @author Duc Nguyen
 */
public class Commit implements Serializable {

    /** The SHA-1 of this commit. */
    private String _uniqueID;

    /** commit message. */
    private String _msg;

    /** timestamp of this commit. */
    private String _timeStamp;

    /** A mapping of file names to blob references. */
    private HashMap<String, String> _filesToBlobs;

    /** The SHA-1 of the first parent. */
    private String _parentHash;

    /** The SHA-1 of the second parent. */
    private String _secondParentHash;

    /** Current branch of this commit. */
    private String _activeBranch;

    /** Current commit's length. */
    private int _length;

    /** Constructor.
     * @param msg the log messsage.
     * @param filesToBlobs the hashmap of files to blobs.
     * @param firstParentHash SHA-1 of the first parent.
     * @param secondParentHash SHA-1 of the second parent.
     * @param activeBranch current branch of this commit. */
    public Commit(String msg, HashMap<String, String> filesToBlobs,
                  String firstParentHash,
                  String secondParentHash, String activeBranch) {
        _msg = msg;
        _filesToBlobs = filesToBlobs;
        _parentHash = firstParentHash;
        Date currDate = new Date();
        DateFormat formatDate = new
                SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy Z");
        formatDate.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        _timeStamp = formatDate.format(currDate);
        byte[] thisToByte = Utils.serialize(this);
        _uniqueID =  Utils.sha1((Object) thisToByte);
        _parentHash = firstParentHash;
        _secondParentHash = secondParentHash;
        _activeBranch = activeBranch;
        if (_parentHash.equals("") && _secondParentHash.equals("")) {
            _length = 0;
        }
    }

    /** Set active branch to BRANCH.
     * @param branch the branch to set this commit's branch to. */
    public void setBranch(String branch) {
        _activeBranch = branch;
    }

    /** Set second parent.
     * @param secondParentHash set the SHA-1 of the second parent. */
    public void setSecondParent(String secondParentHash) {
        _secondParentHash = secondParentHash;
    }

    /** Increase this commit's length.
     * @param length the length to assign current length to. */
    public void setLength(int length) {
        _length = length;
    }

    /** Get the hash value of the current commit.
     * @return String. */
    public String getID() {
        return _uniqueID;
    }

    /** Get the current commit's message.
     * @return String. */
    public String getMsg() {
        return _msg;
    }

    /** Get timestamp of the current commit.
     * @return String. */
    public String getTime() {
        return _timeStamp;
    }

    /** Get length of the current commit.
     * @return int. */
    public int getLength() {
        return _length;
    }

    /** Get mapping of files to blob references.
     * @return HashMap<String, String>. */
    public HashMap<String, String> getFilesBlobsMap() {
        return _filesToBlobs;
    }

    /** Get hash value of first parent.
     * @return String. */
    public String getParentHash() {
        return _parentHash;
    }

    /** Get hash value of second parent.
     * @return String. */
    public String getSecondParentHash() {
        return _secondParentHash;
    }

    /** Get the branch that this commit belongs to.
     * @return String. */
    public String getActiveBranch() {
        return _activeBranch;
    }

    /** Get a blob associated with a file.
     * @param fileName the file name.
     * @return String. */
    public String getBlob(String fileName) {
        if (!_filesToBlobs.isEmpty() && _filesToBlobs.containsKey(fileName)) {
            return _filesToBlobs.get(fileName);
        }
        return "";
    }

    /** Check if a file is tracked in the current commit.
     * @param fileName the file name.
     * @return boolean. */
    public boolean isTracked(String fileName) {
        if (!_filesToBlobs.isEmpty()) {
            return _filesToBlobs.containsKey(fileName);
        }
        return false;
    }

    /** Track a file with FILENAME and SHA1.
     * @param fileName the file name.
     * @param sha1 the SHA-1 value associated with the file. */
    public void track(String fileName, String sha1) {
        getFilesBlobsMap().put(fileName, sha1);
    }

    /** Untrack a file with FILENAME.
     * @param fileName the file name. */
    public void untrack(String fileName) {
        if (!getFilesBlobsMap().isEmpty()) {
            getFilesBlobsMap().remove(fileName);
        }
    }

    /** String that represents the current commit in log.
     * @return String. */
    public String toLog() {
        String start = "===\n";
        String commitID = "commit " + _uniqueID + "\n";
        String date = "Date: " + _timeStamp + "\n";
        String msg = "";
        if (_msg.equals("initial commit")) {
            msg = _msg;
        } else {
            msg = _msg + "\n";
        }
        if (getSecondParentHash().equals("")) {
            return start + commitID + date + msg;
        } else {
            String firstParentSeven = getParentHash().substring(0, 7);
            String secondParentSeven = getSecondParentHash().substring(0, 7);
            String merge = "Merge: " + firstParentSeven + " "
                    + secondParentSeven + "\n";
            return start + commitID + merge + date + msg;
        }
    }
}

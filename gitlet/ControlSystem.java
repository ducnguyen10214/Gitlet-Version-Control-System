package gitlet;

import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedList;

/** Class represents the version-control system.
 * @author Duc Nguyen
 */
public class ControlSystem implements Serializable {

    /** Current system's staging area. */
    private StagingArea _stagingArea = new StagingArea();

    /** Path to current working directory. */
    private final String _workDir = System.getProperty("user.dir");

    /** folder ".gitlet". */
    private final File _pathGitlet = Utils.join(_workDir, ".gitlet");

    /** folder "commits". */
    private final File _pathCommits =
            Utils.join(_pathGitlet, "commits");

    /** folder "branches". */
    private final File _pathBranches =
            Utils.join(_pathGitlet, "branches");

    /** folder "blobs". */
    private final File _pathBlobs =
            Utils.join(_pathGitlet, "blobs");

    /** save remote repo and its directory. */
    private HashMap<String, String> _repos = new HashMap<>();

    /** adequate number of characters to not use abbreviation. */
    private final int _adequate = 40;

    /** Constructor. */
    public ControlSystem() {
        File newSystem = new File(_pathGitlet + "/" + "SYSTEM");
        if (newSystem.exists()) {
            ControlSystem ctrlSys =
                    Utils.readObject(new File(_pathGitlet
                            + "/" + "SYSTEM"), ControlSystem.class);
            cloneSystem(ctrlSys);
        }
    }

    /** Deep copy the CTRLSYS ControlSystem into the current system.
     * @param ctrlSys the other ControlSystem object. */
    public void cloneSystem(ControlSystem ctrlSys) {
        this._stagingArea = ctrlSys._stagingArea;
        this._repos = ctrlSys._repos;
    }

    /** Get current commit.
     * @return Commit. */
    public Commit getCurrentCommit() {
        Commit c;
        File inFile = new File(_workDir + "/.gitlet/branches/HEAD");
        c = Utils.readObject(inFile, Commit.class);
        return c;
    }

    /** Check if a gitlet directory exists.
     * @return boolean. */
    public boolean checkInitialized() {
        return _pathGitlet.exists();
    }

    /** Handles the init command. */
    public void init() throws IOException {
        if (checkInitialized()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            File currSystem = new File(_pathGitlet + "/" + "SYSTEM");
            _pathGitlet.mkdir();
            _pathBranches.mkdir();
            _pathBlobs.mkdir();
            _pathCommits.mkdir();
            Commit initial = new Commit("initial commit",
                    new HashMap<String, String>(), "",
                    "", "master");
            Utils.writeObject(new File(_pathCommits.getPath()
                    + "/" + initial.getID()), initial);
            Utils.writeObject(new File(_pathBranches.getPath()
                    + "/" + "HEAD"), initial);
            Utils.writeObject(new File(_pathBranches.getPath()
                    + "/" + "master"), initial);
            currSystem.createNewFile();
        }
    }

    /** Handles the add command.
     * @param fileName the file name. */
    public void add(String fileName) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        File toBeUpdated = new File(_workDir + "/" + fileName);
        if (toBeUpdated.exists()) {
            byte[] fileBlob = Utils.readContents(toBeUpdated);
            String blobSHA1 = Utils.sha1((Object) fileBlob);
            String tmp = getCurrentCommit().getBlob(fileName);
            if (tmp.equals("") || !tmp.equals(blobSHA1)) {
                _stagingArea.stage(fileName, blobSHA1);
            } else {
                _stagingArea.unstage(fileName);
                _stagingArea.unremove(fileName);
            }
        } else {
            System.out.println("File does not exist.");
        }
    }

    /** Handles the commit command.
     * @param msg the message of the commit.
     * @param secondParentHash the SHA-1 of the second parent. */
    public void commit(String msg, String secondParentHash) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (_stagingArea.isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
        } else {
            Commit newCommit;
            if (!secondParentHash.equals("")) {
                newCommit = new Commit(msg,
                        getCurrentCommit().getFilesBlobsMap(),
                        getCurrentCommit().getID(), secondParentHash,
                        getCurrentCommit().getActiveBranch());
            } else {
                newCommit = new Commit(msg,
                        getCurrentCommit().getFilesBlobsMap(),
                        getCurrentCommit().getID(), "",
                        getCurrentCommit().getActiveBranch());
            }
            newCommit.setLength(getCurrentCommit().getLength() + 1);
            for (String fileName : _stagingArea.getFilesSHA1Map().keySet()) {
                String contentAsString =
                        Utils.readContentsAsString(new
                                File(_workDir + "/" + fileName));
                File newBlob = Utils.join(_pathBlobs + "/"
                        + _stagingArea.getSHA1(fileName));
                try {
                    newBlob.createNewFile();
                } catch (IOException excp) {
                    System.out.println(excp);
                }
                Utils.writeContents(newBlob, contentAsString);
                newCommit.track(fileName, _stagingArea.getSHA1(fileName));
            }
            for (String fileName : _stagingArea.getToBeRemoved()) {
                newCommit.untrack(fileName);
            }
            Utils.writeObject(new File(_pathCommits.getPath()
                    + "/" + newCommit.getID()), newCommit);
            if (getCurrentCommit().getActiveBranch().equals("master")) {
                Utils.writeObject(new File(_pathBranches.getPath()
                        + "/" + "master"), newCommit);
            } else {
                Utils.writeObject(new File(_pathBranches.getPath()
                        + "/" + getCurrentCommit().getActiveBranch()),
                        newCommit);
            }
            Utils.writeObject(new File(_pathBranches.getPath()
                    + "/" + "HEAD"), newCommit);
            _stagingArea.clear();
        }
    }

    /** Handles the rm command.
     * @param fileName the file name. */
    public void rm(String fileName) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (!_stagingArea.checkStaged(fileName)
                && !getCurrentCommit().isTracked(fileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }
        if (_stagingArea.checkStaged(fileName)) {
            _stagingArea.unstage(fileName);
        }
        if (getCurrentCommit().isTracked(fileName)) {
            Utils.restrictedDelete(fileName);
            _stagingArea.stageToBeRemoved(fileName);
        }
    }

    /** Displays the log tree from newest to oldest. */
    public void log() {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        Commit currCommit = getCurrentCommit();
        while (currCommit != null) {
            System.out.println(currCommit.toLog());
            String firstParent = currCommit.getParentHash();
            if (firstParent.equals("")) {
                return;
            }
            File inFile = new File(_pathCommits + "/" + firstParent);
            currCommit = Utils.readObject(inFile, Commit.class);
        }
    }

    /** Displays information about all commits ever made. */
    public void globalLog() {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        for (String commitName : Utils.plainFilenamesIn(_pathCommits)) {
            Commit c = Utils.readObject(new
                    File(_pathCommits + "/" + commitName),
                    Commit.class);
            System.out.println(c.toLog());
        }
    }

    /** Find and return ids of all commits with COMMITMESSAGE.
     * @param commitMessage the commit message we need to find. */
    public void find(String commitMessage) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        boolean found = false;
        for (String commitName : Utils.plainFilenamesIn(_pathCommits)) {
            Commit c = Utils.readObject(new
                    File(_pathCommits + "/" + commitName),
                    Commit.class);
            if (c.getMsg().equals(commitMessage)) {
                found = true;
                System.out.println(c.getID());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Get staged files for status to display.
     * @return String[]. */
    public String[] getStaged() {
        String[] staged = _stagingArea.getFilesSHA1Map().keySet().
                toArray(new String[0]);
        Arrays.sort(staged);
        return staged;
    }

    /** Get removed files for status to display.
     * @return String[]. */
    public String[] getRemoved() {
        String[] removed = _stagingArea.getToBeRemoved().toArray(new String[0]);
        Arrays.sort(removed);
        return removed;
    }

    /** Get modified but not staged files for status to display.
     * @return ArrayList<String>. */
    public HashMap<String, String> getMod() {
        HashMap<String, String> mod = new HashMap<>();
        for (String file : Utils.plainFilenamesIn(_workDir)) {
            if (file.endsWith(".txt")) {
                File tmp = new File(_workDir + "/" + file);
                if (tmp.exists()) {
                    byte[] fileBlob = Utils.readContents(tmp);
                    String fileSHA = Utils.sha1((Object) fileBlob);
                    if (getCurrentCommit().isTracked(file)
                            && !getCurrentCommit().getBlob(file).equals(fileSHA)
                            && !_stagingArea.checkStaged(file)) {
                        mod.put(file, "(modified)");
                    } else if (_stagingArea.checkStaged(file)
                            && !_stagingArea.getSHA1(file).equals(fileSHA)) {
                        mod.put(file, "(modified)");
                    }
                } else if (!tmp.exists()) {
                    if (_stagingArea.checkStaged(file)) {
                        mod.put(file, "(deleted)");
                    } else if (getCurrentCommit().isTracked(file)
                            && !_stagingArea.checkRemoved(file)) {
                        mod.put(file, "(deleted)");
                    }
                }
            }
        }
        for (String file : getCurrentCommit().getFilesBlobsMap().keySet()) {
            if (!mod.containsKey(file) && !_stagingArea.checkRemoved(file)) {
                File tmp = new File(_workDir + "/" + file);
                if (!tmp.exists()) {
                    mod.put(file, "(deleted)");
                }
            }
        }
        return mod;
    }

    /** Get untracked files for status to display.
     * @return ArrayList<String>. */
    public ArrayList<String> getUntracked() {
        ArrayList<String> untracked = new ArrayList<>();
        for (String file : Utils.plainFilenamesIn(_workDir)) {
            if (file.endsWith(".txt")) {
                File tmp = new File(_workDir + "/" + file);
                if (tmp.exists() && !_stagingArea.checkStaged(file)
                        && !getCurrentCommit().isTracked(file)) {
                    untracked.add(file);
                }
            }
        }
        Arrays.sort(untracked.toArray());
        return untracked;
    }

    /** Display the status of the current control system. */
    public void status() {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        StringBuilder branches = new StringBuilder("=== Branches ===\n");
        StringBuilder stagedFiles = new StringBuilder("=== Staged Files ===\n");
        StringBuilder removedFiles = new
                StringBuilder("=== Removed Files ===\n");
        StringBuilder modNotStaged = new
                StringBuilder("=== Modifications Not Staged For Commit ===\n");
        StringBuilder untrackedFiles = new
                StringBuilder("=== Untracked Files ===\n");
        Map<String, String> modFiles = new TreeMap<>(getMod());
        for (String branchName : Utils.plainFilenamesIn(_pathBranches)) {
            String currBranch = getCurrentCommit().getActiveBranch();
            if (!branchName.equals("HEAD")) {
                if (branchName.equals(currBranch)) {
                    branches.append("*").append(branchName).append("\n");
                } else {
                    branches.append(branchName).append("\n");
                }
            }
        }
        for (String file : getStaged()) {
            stagedFiles.append(file).append("\n");
        }

        for (String file : getRemoved()) {
            removedFiles.append(file).append("\n");
        }

        for (String file : modFiles.keySet()) {
            modNotStaged.append(file).append(" ")
                    .append(modFiles.get(file)).append("\n");
        }
        for (String file : getUntracked()) {
            untrackedFiles.append(file).append("\n");
        }
        System.out.println(branches + "\n" + stagedFiles + "\n"
                + removedFiles + "\n" + modNotStaged + "\n" + untrackedFiles);
    }

    /** Return the full 40-digit commit ID from ABBREVIATED.
     * @param abbreviated the abbreviated ID.
     * @return String. */
    public String translate(String abbreviated) {
        String commitName = abbreviated;
        if (commitName.length() < _adequate) {
            for (String name : Utils.plainFilenamesIn(_pathCommits)) {
                String abbreviatedName = name.substring(0,
                        abbreviated.length());
                if (commitName.equals(abbreviatedName)) {
                    commitName = name;
                    break;
                }
            }
        }
        return commitName;
    }

    /** Handle the use case of checkout with file name.
     * @param fileName the file name. */
    public void checkoutFile(String fileName) {
        if (getCurrentCommit().isTracked(fileName)) {
            String headBlob = getCurrentCommit().getBlob(fileName);
            String contents = Utils.readContentsAsString(new
                    File(_pathBlobs + "/" + headBlob));
            Utils.writeContents(new
                    File(_workDir + "/" + fileName), contents);
        }
    }

    /** Handle the use case of checkout with commitID and file name.
     * @param c the commit name.
     * @param fileName the file name.
     */
    public void checkoutCommitFile(String c, String fileName) {
        String commitName = translate(c);
        try {
            Commit wantCommit = Utils.readObject(new
                            File(_pathCommits + "/" + commitName),
                    Commit.class);
            if (!wantCommit.isTracked(fileName)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String commitBlob = wantCommit.getBlob(fileName);
            String contents = Utils.readContentsAsString(new
                    File(_pathBlobs + "/" + commitBlob));
            Utils.writeContents(new File(_workDir
                    + "/" + fileName), contents);
        } catch (IllegalArgumentException excp) {
            System.out.println("No commit with that id exists.");
        }
    }

    /** Get files that are supposed to be overwritten during checkout.
     * @param wantBranch the branch given.
     * @return HashMap<String, String>. */
    public HashMap<String, String> getOverwrites(Commit wantBranch) {
        HashMap<String, String> overwrites = new HashMap<>();
        for (String file : wantBranch.getFilesBlobsMap().keySet()) {
            if (getCurrentCommit().isTracked(file)) {
                overwrites.put(file, wantBranch.getBlob(file));
            }
        }
        return overwrites;
    }

    /** Get files that are new during checkout.
     * @param wantBranch the branch given.
     * @return HashMap<String, String>. */
    public HashMap<String, String> getNewFiles(Commit wantBranch) {
        HashMap<String, String> newFiles = new HashMap<>();
        for (String file : wantBranch.getFilesBlobsMap().keySet()) {
            if (!getCurrentCommit().isTracked(file)) {
                newFiles.put(file, wantBranch.getBlob(file));
            }
        }
        return newFiles;
    }

    /** Get files that are removed during checkout.
     * @param wantBranch the branch given.
     * @return ArrayList<String>. */
    public ArrayList<String> getRemoves(Commit wantBranch) {
        ArrayList<String> removes = new ArrayList<>();
        for (String file : Utils.plainFilenamesIn(_workDir)) {
            if (file.endsWith(".txt") && getCurrentCommit().
                    isTracked(file) && !wantBranch.isTracked(file)) {
                removes.add(file);
            }
        }
        return removes;
    }

    /** Check for untracked files in the current working directory.
     * @param wantBranch the commit we're comparing to, or can be null.
     * @return boolean. */
    public boolean checkUntrackedDir(Commit wantBranch) {
        for (String file : Utils.plainFilenamesIn(_workDir)) {
            if (wantBranch != null) {
                if (file.endsWith(".txt") && !getCurrentCommit().isTracked(file)
                        && wantBranch.isTracked(file)) {
                    System.out.println("There is an untracked file in the way"
                            + "; delete it, or add"
                            + " and commit it first.");
                    return false;
                }
            } else {
                if (file.endsWith(".txt")
                        && !getCurrentCommit().isTracked(file)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add" + " and commit it first.");
                    return false;
                }
            }
        }
        return true;
    }

    /** Handle the checkout command.
     * @param args an array of String for checkout operands. */
    public void checkout(String... args) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (args.length == 3) {
            checkoutFile(args[2]);
        } else if (args.length == 4) {
            checkoutCommitFile(args[1], args[3]);
        } else if (args.length == 2) {
            if (args[1].contains("/")) {
                args[1] = args[1].replaceAll("\\/", "_");
            }
            if (!new File(_pathBranches + "/" + args[1]).exists()) {
                System.out.println("No such branch exists.");
                return;
            }
            Commit wantBranch = Utils.readObject(new
                    File(_pathBranches + "/" + args[1]), Commit.class);
            if (wantBranch.getID().equals(getCurrentCommit().getID())
                    && wantBranch.getActiveBranch().equals(getCurrentCommit()
                    .getActiveBranch())) {
                System.out.println("No need to check out the current branch.");
                return;
            }
            if (!checkUntrackedDir(wantBranch)) {
                return;
            }
            for (String file : getNewFiles(wantBranch).keySet()) {
                File newFile = new File(_workDir + "/" + file);
                try {
                    newFile.createNewFile();
                } catch (IOException excp) {
                    System.out.println(excp);
                }
                String blob = getNewFiles(wantBranch).get(file);
                String contents = Utils.readContentsAsString(new
                        File(_pathBlobs + "/" + blob));
                Utils.writeContents(newFile, contents);
            }
            for (String file : getOverwrites(wantBranch).keySet()) {
                String blob = getOverwrites(wantBranch).get(file);
                String contents = Utils.readContentsAsString(new
                        File(_pathBlobs + "/" + blob));
                Utils.writeContents(new File(_workDir + "/"
                        + file), contents);
            }
            for (String file : getRemoves(wantBranch)) {
                Utils.restrictedDelete(file);
            }
            _stagingArea.clear();
            Utils.writeObject(new File(_pathBranches + "/"
                    + "HEAD"), wantBranch);
        }
    }

    /** Handles the branch command.
     * @param branchName the branch name. */
    public void branch(String branchName) throws IOException {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        File newBranch = new File(_pathBranches + "/" + branchName);
        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
        }
        newBranch.createNewFile();
        Commit commitHead = Utils.readObject(new
                File(_pathBranches + "/" + "HEAD"), Commit.class);
        commitHead.setBranch(branchName);
        Utils.writeObject(newBranch, commitHead);
    }

    /** Delete a branch.
     * @param branchName the branch name. */
    public void rmBranch(String branchName) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        boolean found = false;
        for (String branch : Utils.plainFilenamesIn(_pathBranches)) {
            if (branch.equals(branchName)) {
                if (getCurrentCommit().getActiveBranch().equals(branch)) {
                    System.out.println("Cannot remove the current branch");
                } else {
                    new File(_pathBranches + "/" + branch).delete();
                }
                found = true;
            }
        }
        if (!found) {
            System.out.println("A branch with that name does not exist.");
        }
    }

    /** Get redundant.
     * @param wantCommit the commit given.
     * @return ArrayList<String>. */
    public ArrayList<String> getRedundants(Commit wantCommit) {
        ArrayList<String> redundantFiles = new ArrayList<>();
        for (String file : wantCommit.getFilesBlobsMap().keySet()) {
            if (!new File(_workDir + "/" + file).exists()) {
                redundantFiles.add(file);
            }
        }
        return redundantFiles;
    }

    /** Handles the reset command.
     * @param commitID the commitID to revert to. */
    public void reset(String commitID) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (!new File(_pathCommits + "/" + commitID).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }
        String commitName = translate(commitID);
        Commit wantCommit = Utils.readObject(new
                File(_pathCommits + "/" + commitName), Commit.class);
        if (!checkUntrackedDir(wantCommit)) {
            return;
        }
        for (String file : getOverwrites(wantCommit).keySet()) {
            String blob = getOverwrites(wantCommit).get(file);
            String contents = Utils.readContentsAsString(new
                    File(_pathBlobs + "/" + blob));
            Utils.writeContents(new File(_workDir + "/" + file),
                    contents);
        }
        for (String file : getRemoves(wantCommit)) {
            Utils.restrictedDelete(file);
        }

        for (String file : getRedundants(wantCommit)) {
            wantCommit.untrack(file);
        }
        _stagingArea.clear();
        Utils.writeObject(new File(_pathBranches + "/" + "HEAD"),
                wantCommit);
        Utils.writeObject(new File(_pathBranches + "/"
                + getCurrentBranch()), wantCommit);
    }

    /** Get the current branch.
     * @return String. */
    public String getCurrentBranch() {
        return getCurrentCommit().getActiveBranch();
    }

    /** Get all the ancestors of a branch.
     * @param c the commit (branch).
     * @return LinkedList<Commit>. */
    public LinkedList<Commit> getAncestors(Commit c) {
        LinkedList<Commit> ancestors = new LinkedList<>();
        ancestors.add(c);
        while (!c.getParentHash().equals("")) {
            ancestors.add(Utils.readObject(new
                    File(_pathCommits + "/" + c.getParentHash()),
                    Commit.class));
            if (!c.getSecondParentHash().equals("")
                    && new File(_pathCommits + "/"
                    + c.getSecondParentHash()).exists()) {
                ancestors.add(Utils.readObject(new File(_pathCommits
                        + "/" + c.getSecondParentHash()), Commit.class));
            }
            c = Utils.readObject(new File(_pathCommits + "/"
                    + c.getParentHash()), Commit.class);
        }
        return ancestors;
    }

    /** Find a latest common ancestor of two branches.
     * @param givenBranch the given branch.
     * @return Commit. */
    public Commit findSplit(Commit givenBranch) {
        LinkedList<Commit> currAncestors = getAncestors(getCurrentCommit());
        LinkedList<Commit> branchAncestors = getAncestors(givenBranch);
        for (int i = 0; currAncestors.size() > branchAncestors.size(); i += 1) {
            currAncestors.poll();
        }
        for (int i = 0; branchAncestors.size() > currAncestors.size(); i += 1) {
            branchAncestors.poll();
        }
        while (!currAncestors.isEmpty() && !branchAncestors.isEmpty()) {
            if (currAncestors.peekFirst().getLength()
                    != branchAncestors.peekFirst().getLength()) {
                if (currAncestors.size() > branchAncestors.size()) {
                    currAncestors.poll();
                } else if (currAncestors.size() < branchAncestors.size()) {
                    branchAncestors.poll();
                } else {
                    currAncestors.poll();
                    branchAncestors.poll();
                }
            } else if (currAncestors.peekFirst().getLength()
                    == branchAncestors.peekFirst().getLength()
                    && !currAncestors.peekFirst().getID()
                    .equals(branchAncestors.peekFirst().getID())) {
                branchAncestors.poll();
            }
            if (currAncestors.peekFirst().getID()
                    .equals(branchAncestors.peekFirst().getID())) {
                return currAncestors.poll();
            }
        }
        return null;
    }

    /** Handle conflict when merging two versions.
     * @param fileName the file name.
     * @param contentCurrentBranch the content of the file in current branch.
     * @param contentGivenBranch the content of the file in given branch. */
    public void conflictHandler(String fileName, String contentCurrentBranch,
                                String contentGivenBranch) {
        StringBuilder top = new StringBuilder("<<<<<<< HEAD\n");
        StringBuilder placeHolder = new StringBuilder("=======\n");
        StringBuilder bot = new StringBuilder(">>>>>>>\n");
        String newContents = top + contentCurrentBranch + placeHolder
                + contentGivenBranch + bot;
        Utils.writeContents(new File(_workDir + "/" + fileName),
                newContents);
        add(fileName);
    }

    /** Handle files tracked in the wanted branch when merging.
     * Return true if there was conflict during merging.
     * @param wantedBranch the given branch.
     * @param split the latest common ancestor.
     * @param curr the current branch.
     * @return boolean. */
    public boolean mergeWantedBranch(Commit wantedBranch, Commit split,
                                  Commit curr) {
        for (String file : wantedBranch.getFilesBlobsMap().keySet()) {
            if (wantedBranch.isTracked(file)
                    && curr.isTracked(file)
                    && split.isTracked(file)
                    && !wantedBranch.getBlob(file).equals(split.getBlob(file))
                    && curr.getBlob(file).equals(split.getBlob(file))) {
                String blob = wantedBranch.getBlob(file);
                String contents = Utils.readContentsAsString(new
                        File(_pathBlobs + "/" + blob));
                if (!new File(_workDir + "/" + file).exists()) {
                    try {
                        new File(_workDir + "/" + file)
                                .createNewFile();
                    } catch (IOException excp) {
                        System.out.println(excp);
                    }
                }
                Utils.writeContents(new File(_workDir + "/" + file),
                        contents);
            } else if (!split.isTracked(file) && !curr.isTracked(file)) {
                String blob = wantedBranch.getBlob(file);
                String contents = Utils.readContentsAsString(new
                        File(_pathBlobs + "/" + blob));
                if (!new File(_workDir + "/" + file).exists()) {
                    try {
                        new File(_workDir + "/" + file).
                                createNewFile();
                    } catch (IOException excp) {
                        System.out.println(excp);
                    }
                }
                Utils.writeContents(new File(_workDir + "/" + file),
                        contents);
                add(file);
            } else if (curr.isTracked(file)
                    && !curr.getBlob(file).equals(wantedBranch.getBlob(file))
                    && (!split.isTracked(file)
                    || (!split.getBlob(file).equals(curr.getBlob(file)))
                    && !wantedBranch.getBlob(file)
                        .equals(split.getBlob(file)))) {
                String contentCurrentBranch = Utils.readContentsAsString(new
                        File(_pathBlobs + "/"
                        + getCurrentCommit().getBlob(file)));
                String contentGivenBranch = Utils.readContentsAsString(new
                        File(_pathBlobs + "/"
                        + wantedBranch.getBlob(file)));
                conflictHandler(file, contentCurrentBranch, contentGivenBranch);
                return true;
            }
        }
        return false;
    }

    /** Handle files tracked by the current branch during merging.
     * Return true if there was conflict happening.
     * @param wantedBranch the given branch.
     * @param split the latest common ancestor.
     * @return boolean.
     */
    public boolean mergeCurrentBranch(Commit wantedBranch, Commit split) {
        for (String file : getCurrentCommit().getFilesBlobsMap().keySet()) {
            if (split.isTracked(file)
                    && split.getBlob(file).equals(getCurrentCommit()
                    .getBlob(file))
                    && !wantedBranch.isTracked(file)) {
                Utils.restrictedDelete(file);
                _stagingArea.stageToBeRemoved(file);
            } else if (split.isTracked(file) && !wantedBranch.isTracked(file)) {
                String contentCurrentBranch = Utils.readContentsAsString(new
                        File(_pathBlobs + "/"
                        + getCurrentCommit().getBlob(file)));
                String contentGivenBranch = "";
                conflictHandler(file, contentCurrentBranch, contentGivenBranch);
                return true;
            }
        }
        return false;
    }

    /** Handle the merge command.
     * @param branchName the name of the branch we want to merge with. */
    public void merge(String branchName) {
        if (!checkInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (!new File(_pathBranches + "/" + branchName).exists()) {
            System.out.println("A branch with that name does not exits.");
            return;
        }
        if (!_stagingArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        if (!checkUntrackedDir(null)) {
            return;
        }
        Commit wantedBranch = Utils.readObject(new
                File(_pathBranches + "/" + branchName), Commit.class);
        if (wantedBranch.getID().equals(getCurrentCommit().getID())) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit split = findSplit(wantedBranch);
        boolean conflict = false;
        if (split.getID().equals(wantedBranch.getID())) {
            System.out.println("Given branch is an ancestor of "
                    + "the current branch.");
        } else if (split.getID().equals(getCurrentCommit().getID())) {
            checkout("checkout", branchName);
            System.out.println("Current branch fast-forwarded.");
        } else {
            if (mergeWantedBranch(wantedBranch, split, getCurrentCommit())) {
                conflict = true;
            }
            if (mergeCurrentBranch(wantedBranch, split)) {
                conflict = true;
            }
            if (branchName.contains("_")) {
                branchName = branchName.replaceAll("_", "/");
            }
            commit("Merged " + branchName + " into "
                    + getCurrentBranch() + ".", wantedBranch.getID());
            if (conflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }
    }

    /** Handle the `add-remote` command.
     * @param remoteDir name of the remote directory.
     * @param remoteName the remote name. */
    public void addRemote(String remoteName, String remoteDir) {
        if (!_repos.isEmpty() && _repos.containsKey(remoteName)) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        _repos.put(remoteName, remoteDir);
    }

    /** Handle the `rm-remote` command.
     * @param remoteName the remote name. */
    public void rmRemote(String remoteName) {
        if (!_repos.containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
        _repos.remove(remoteName);
    }

    /** Handle the `push` command.
     * @param remoteName the remote name.
     * @param remoteBranchName the remote branch name. */
    public void push(String remoteName, String remoteBranchName)
            throws IOException {
        if (!_repos.containsKey(remoteName)
                || !new File(_repos.get(remoteName)).exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File pathToBranchRM = new File(_repos.get(remoteName)
                + "/branches/" + remoteBranchName);
        if (!pathToBranchRM.exists()) {
            pathToBranchRM.createNewFile();
            Utils.writeContents(pathToBranchRM, getCurrentCommit());
        } else {
            Commit remoteBranchHead = Utils.readObject(pathToBranchRM,
                    Commit.class);
            Commit currCommit = getCurrentCommit();
            HashMap<String, Commit> futureCommits = new HashMap<>();
            boolean inHistory = false;
            while (currCommit != null) {
                if (currCommit.getID().equals((remoteBranchHead).getID())) {
                    inHistory = true;
                    break;
                }
                futureCommits.put(currCommit.getID(), currCommit);
                String firstParent = currCommit.getParentHash();
                if (firstParent.equals("")) {
                    break;
                }
                currCommit = Utils.readObject(new File(_pathCommits
                        + "/" + firstParent), Commit.class);
            }
            if (!inHistory) {
                System.out.println("Please pull down remote "
                        + "changes before pushing.");
                return;
            }
            for (String commitID : futureCommits.keySet()) {
                File tmp = new File(_repos.get(remoteName)
                        + "/commits/" + commitID);
                tmp.createNewFile();
                Utils.writeObject(tmp, futureCommits.get(commitID));
            }
            Utils.writeObject(new File(_repos.get(remoteName)
                    + "/branches/HEAD"), getCurrentCommit());
        }
    }

    /** Handle the `fetch` command.
     * @param remoteName the remote name.
     * @param remoteBranchName the remote branch name. */
    public void fetch(String remoteName, String remoteBranchName)
            throws IOException {
        if (!_repos.containsKey(remoteName)
                || !new File(_repos.get(remoteName)).exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File pathToBranchRM = new File(_repos.get(remoteName)
                + "/branches/" + remoteBranchName);
        if (!pathToBranchRM.exists()) {
            System.out.println("That remote does not have that branch.");
            return;
        }
        File pathCommitRM = new File(_repos.get(remoteName)
                + "/commits");
        for (File f : pathCommitRM.listFiles()) {
            Commit rm = Utils.readObject(f, Commit.class);
            if (rm.getActiveBranch().equals(remoteBranchName)) {
                String rmID = rm.getID();
                File local = new File(_pathCommits + "/" + rmID);
                if (!local.exists()) {
                    local.createNewFile();
                    Utils.writeObject(local, rm);
                }
                for (String file : rm.getFilesBlobsMap().keySet()) {
                    String blobSHA1 = rm.getBlob(file);
                    String contents = Utils.readContentsAsString(new
                            File(_repos.get(remoteName)
                            + "/blobs/" + blobSHA1));
                    File localBlob = new File(_pathBlobs + "/"
                            + blobSHA1);
                    if (!localBlob.exists()) {
                        localBlob.createNewFile();
                        Utils.writeObject(localBlob, contents);
                    }
                }
            }
        }
        String tmp = remoteName + "_" + remoteBranchName;
        File localRMBranch = new File(_pathBranches + "/" + tmp);
        if (!localRMBranch.exists()) {
            localRMBranch.createNewFile();
        }
        Commit rmBranch = Utils.readObject(pathToBranchRM, Commit.class);
        rmBranch.setBranch(getCurrentBranch());
        Utils.writeObject(localRMBranch, rmBranch);
    }

    /** Handle the `pull` command.
     * @param remoteName the remote name.
     * @param remoteBranchName the remote branch name. */
    public void pull(String remoteName, String remoteBranchName)
            throws IOException {
        fetch(remoteName, remoteBranchName);
        String tmp = remoteName + "_" + remoteBranchName;
        merge(tmp);
    }
}

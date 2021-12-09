# Gitlet Design Document

**Name**: Duc Nguyen

## Classes and Data Structures
### Class Commit
- This class represents each commit of the version-control system.
- Implements `Serializable`.
#### Fields
1. `String _uniqueID`: the SHA-1 value of this commit.
2. `String _msg`: the message goes with this commit.
3. `String _timeStamp`: the timeStamp of this commit.
4. `HashMap<String, String> _filesToBlobs`: a mapping of file names to the blobs in `./gitlet/blobs`.
5. `String _parentHash`: the SHA-1 value of the first parent.
6. `String _secondParentHash`: the SHA-1 value of the second parent.
### Class StagingArea
- This class represents the staging area.
- Implements `Serializable`.
#### Fields
1. `ArrayList<String> _toBeRemoved`: list of old files to be removed.
2. `HashMap<String, String> _newFiles`: a mapping of new files to their SHA-1.
### Class ControlSystem
- This class represents our version-control system.
- Implements `Serializable`.
#### Fields
1. `StagingArea _stagingArea`: current control system's staging area.
2. `String _workDir`: the current working directory of this repo.
3. `File _pathGitlet`: folder `.gitlet` in the current working directory.
4. `File _pathCommits`: folder `commits` in the current working directory.
5. `File _pathBranches`: folder `branches` in the current working directory.
6. `File _pathBlobs`: folder `blobs` in the current working directory.

## Algorithms
### Class Commit
1. `Commit(String msg, HashMap<String, String> filesToBlobs, String firstParentHash, String secondParentHash)`: the class constructor.
   Create a commit with the given message, and a map of file-to-blob-reference as given. Add timestamp,
   and serialize this to generate its own SHA-1. Then save hash references of first parent and second parent.
2. `String getID()`: return field `_uniqueID` of this commit.
3. `String getMsg()`: return the current commit's message.
4. `String getTime()`: return the current commit's timestamp.
5. `HashMap<String, String> getFilesBlobsMap()`: return the mapping of file names to their blobs.
6. `String getParentHash()`: return the SHA-1 of the first parent.
7. `String getSecondParentHash()`: return the SHA-1 fo the second parent.
8. `String toLog()`: return the string that represents the current commit when using command `log`.
### Class StagingArea
1. `void stageToBeRemoved(String filename)`: stage a file for removal.
2. `void stage(String fileName, String fileSHA1)`: stage a file.
3. `void unstage(String fileName)`: unstage a file if it is in the area.
4. `void clear()`: clear the staging area.
5. `boolean isEmpty()`: check to see if the staging area is empty, return true if it is.
6. `ArrayList<String> getToBeRemoved()`: return the list of files to be removed.
7. `HashMap<String, String> getFilesSHA1Map()`: return the mapping of new files to their SHA-1's.
### Class ControlSystem
1. `ControlSystem()`: the class constructor. 
   - Check if there is any file called `SYSTEM` in folder `.gitlet` of 
   the current directory. If yes, that means a ControlSystem exists, so deep copy that `SYSTEM` into this system.
2. `deepCopy(ControlSystem ctrlSys)`: deep copy `ctrlSys` into the current control system.
3. `Commit getCurrentCommit()`: return Commit `HEAD` in `.gitlet/commits`.
4. `void init()`: handle the `init` command. 
   - If there is already a `.gitlet` folder available in the current working directory,
   print `A Gitlet version-control system already exists in the current directory.`. 
   - Otherwise, create folders `.gitlet`, `branches`,`blobs`, `commits`, then create a new Commit. 
   - Save the new commit firstly under its SHA-1 value as its name in folder `commits`, then under `HEAD` as its name in folder `branches`, then under `master` as its name in folder `branches`.
5. `add(String fileName)`: handle the `add` command. 
   - First, check if the file exists in the current working directory. 
   - If not, print `File does not exist` and exit. 
   - Otherwise, read contents from the file as a byte array, then generate SHA-1 of the file from that byte array. Next, get the SHA-1 
   corresponding to the file in the current commit. If the file does not have an SHA-1 in current commit or the two SHA-1's are different,
   then stage the file, otherwise unstage it if staged.
6. `commit(String msg)`: handle the `commit` command. 
   - First, if the staging area is empty, print `No changes added to the commit.` and exit. 
   - Else if message is empty, print `Please enter a commit message.` and exit. 
   - Else, create a new commit cloned from the current one, but set its parent to the current commit. Then, for each file in staging area, read the file's contents as string, and create a new blob with the file contents in it. Then, 
   put this new fileName-blob into this current commit's HashMap. Then, for each file to be removed in the staging area, remove them from the 
   current commit's HashMap. Save the new commit to folder `commits`, update `HEAD` and `master` to the new commit.
7. `void log()`: display the log tree in order from newest to oldest.
   - First, get the current commit `HEAD`. Print it out.
   - Then, get the parent hash of the current commit. If it is empty, return because it's hit the root.
   - Otherwise, update current commit with parent commits until we hit the root.
8. `void checkout(String... args)`: handle the `checkout` command.
   - If args has length 3:
     - If the file is contained in the current commit's HashMap, get its blob and read contents in that blob.
     - Overwrite the current file in current working directory with the contents retrieved.
   - If args has length 4:
     - Get the wanted commit from folder `commits`. If can't get, print `No commit with that id exists.`.
     - If the wanted commit does not contain the file, print `File does not exist in that commit` and exit.
     - Else, get the blob of the file in the wanted commit, read its contents, and overwrite the current file in current working directory.
   - If args has length 2:
     - Get the wanted commit from folder `branches`. If can't get, print `No such branch exists.`.
     - If the wanted commit has the same ID with the current commit `HEAD`, print `No need to checkout the current branch.` and return.
     - For each file in the current working directory that has not been tracked by the current commit, 
     print `There is an untracked file in the way; delete it, or add and commit it first.` and return.
     - Create a HashMap to store files to be overwritten by the wanted commit, and a List of files to be removed that do not match with 
     the wanted commit. Then, proceed to overwrite/delete files, clear the staging area, and write the wanted commit in folder `branches`
     as `HEAD`.

## Persistence


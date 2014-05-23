-- runhaskell TestUtils.hs resourcemanager/output/
import Control.Monad         (forM_)
import Data.List
import Data.Maybe
import Data.Tuple
import qualified Data.Map    as M
import System.Directory      (getDirectoryContents)
import System.Environment    (getArgs)
import System.IO
import System.FilePath.Posix (takeFileName)

{-- Used for storing information for the input.conf files --}
data Setting = Setting
  {
    theId     :: Integer
  , probes    :: Integer
  , nodes     :: Integer
  , jobs      :: Integer
  }

{- toString definition for Setting -}
instance Show Setting where
   show setting = "OUTFILE:output"   ++(show (theId setting)) ++".out\n"++
                  "NUMBER_OF_PROBES:"++(show (probes setting))++"\n"    ++
                  "NUMBER_OF_NODES:" ++(show (nodes setting)) ++"\n"    ++
                  "NUMBER_OF_JOBS:"  ++(show (jobs setting))

-- list the commands
data Command    = PRB  | INI | ASN | SCH deriving (Show,Read,Ord,Eq)
data End        = High | Low deriving (Show,Read)
type TimeStamp  = Integer
type JobId      = Integer
type Output     = M.Map Integer [Measure] -- used for saving id [(cmd,time)]
type Measure    = (Command, TimeStamp)
type OutputLine = (JobId,   Measure)
type TestSpec   = [(String,((Command,End),(Command,End)))]

-- the specification of what should be tested
test1 :: TestSpec
test1 = [(,) "Probing"        $ (,) (PRB,Low)  (INI,Low),
         (,) "NetworkLatency" $ (,) (ASN,High) (PRB,Low),
         (,) "WaitingQueue"   $ (,) (SCH,High) (ASN,Low),
         (,) "Total"          $ (,) (SCH,High) (INI,Low)]

test2 :: TestSpec
test2 = [(,) "NetworkLatency" $ (,) (ASN,High) (INI,Low),
         (,) "WaitingQueue"   $ (,) (SCH,High) (ASN,Low),
         (,) "Total"          $ (,) (SCH,High) (INI,Low)]

{- If no arguments are given, Print all combinations 
   for the setting variables probes, nodes, jobs and their ID. -}
main :: IO ()
main = do
   args <- getArgs
   case args of
      [] -> createSettingFiles
      -- for all files in a given directory, do performOutputParsing and
      -- save the result to appendfile.txt
      _  -> do outputFiles <- getDirectoryContents (head args)
               let skipFiles = [".","..",".DS_Store"]
                   spec      = case (head (drop 1 args)) of
                                  "test1" -> test1
                                  "test2" -> test2
               forM_ [(head args)++o|o<-outputFiles,not $ elem o skipFiles]
                     ((flip (performOutputParsing spec)) "resourcemanager/statistics/")
               
{- Generate combinations needed to test and write all files to disk -}
createSettingFiles :: IO ()
createSettingFiles = do
   let numberOfProbes = [1,2,3,5,10]
       numberOfNodes  = [100,1000,10000,100000]
       numberOfJobs   = [1000,10000,100000,1000000,10000000]
       combinations   = [(probe,node,job)|probe<-numberOfProbes,
                                          node<-numberOfNodes,
                                          job<-numberOfJobs]
       idcomb         = [Setting theId probe node job
                        | (theId,(probe,node,job)) <- zip [1..] combinations]
       fileName       = "resourcemanager/settingfiles/input"
   forM_ idcomb $ \x-> writeFileLine (fileName++(show . theId) x++".conf")
                                     (show x)
                                     WriteMode

{- Create a file, write the str given and close the file -}
writeFileLine :: FilePath -> String -> IOMode ->IO ()
writeFileLine fp content mode = do
   handle <- openFile fp mode
   hPutStrLn handle content
   hClose handle

{- Read an output file, parse it, 
   perform calculations and generate new output files -}
performOutputParsing :: TestSpec -> FilePath -> FilePath -> IO ()
performOutputParsing spec readFrom writeTo = do
   file <- readFile readFrom
   let allLines = map parseLine (lines file)
       theMap   = foldl (+->) M.empty allLines
       ls       = map ((,) spec) $ sort $ M.toList theMap
   -- perform calculations, get a big string back and write this
   -- string to the given statistics file
   forM_ [(foldl calculations "" ls,writeTo++(takeFileName readFrom)),
          (averages ls, writeTo++"__averages__"++(takeFileName readFrom))]
         (\(theList,theMsg) -> writeFileLine theMsg theList WriteMode)

{- Get averages and 99th percentile as a String -}
averages :: [(TestSpec,(JobId,[Measure]))] -> String
averages ls =
   concat [label++" averages time "     ++(show $ getAvg $ getTimesFor f t oldLs)++
           "\n" ++label++" 99th p time "++(show $ get99P $ getTimesFor f t oldLs)++"\n"
          | (label,(f,t)) <- (fst (head ls))]
   where oldLs = map snd ls

{- For a list of times, calculate average -}
getAvg :: [TimeStamp] -> TimeStamp
getAvg ls = div (sum ls) (toInteger $ length ls)

{- For a list of times, calculate 99th percentile 
   @see https://answers.yahoo.com/question/index?qid=1005122102489 -}
get99P :: [TimeStamp] -> TimeStamp
get99P ls = last percent99
   where percent99 = take (ceiling $ len*0.99) (sort ls)
         len       = fromIntegral  $ length    (sort ls)

{- Get all results for Command1 - Command2
   
   THIS FUNCTION IS UNSAFE because it assumes all Maybes are Just -}
getTimesFor :: (Command,End) -> (Command,End) -> [(JobId,[Measure])] -> [TimeStamp]
getTimesFor (c1,e1) (c2,e2) ls = [snd m1 - snd m2 | (m1,m2) <- a]
   where a = [(fromJust $ getM c1 e1 meas,fromJust $ getM c2 e2 meas)|(_,meas)<-ls]

{- Here we can perform the calculations needed, and then format it all as
   a string -}
calculations :: String -> (TestSpec,(JobId,[Measure])) -> String
calculations old (spec,(jobId,commLs)) = old ++ show jobId ++ "," ++ combs ++ "\n"
   where combs = concat [(safeCalcMeasure x y commLs) ++ "," | (x,y)<-(map snd spec)]
         safeCalcMeasure (cmd1,e1) (cmd2,e2) commandLs =
            let m1 = getM cmd1 e1 commandLs
                m2 = getM cmd2 e2 commandLs in
            case (isJust m1 && isJust m2) of
               True  -> show $ (snd (fromJust m1)) - (snd (fromJust m2))
               False -> "error_command_not_found"

{- try to pick from Just, in a safe manner... If not found return nothing
   (Command, TimeStamp) -}
getM :: Command -> End -> [Measure] -> Maybe Measure
getM cmd e ls =
   case (isJust $ lookup cmd $ searchList e ls) of
      True  -> Just (head $ searchList e ls)
      False -> Nothing
   where searchList :: End -> [Measure] -> [Measure]
         searchList High inp = reverse $ sortedList inp
         searchList Low  inp = sortedList inp
         sortedList inp      = map swap $ sort [(tS,iD)|(iD,tS)<-inp,iD==cmd]

{- for a line from any output file (from kompics) take the 
   info we need and put it in an OutputLine tuple.
   Expects the format of three words on a line separated by space -}
parseLine :: String -> OutputLine
parseLine inp = (read jobId::JobId,(read command::Command,read timest::TimeStamp))
      where [command,jobId,timest] = words inp
         
{- add one item to the output hashmap
   fst line is the jobId, snd line are the measures for the outputline -}
(+->) :: Output -> OutputLine -> Output
out +-> line = M.insertWith (++) (fst line) [snd line] out

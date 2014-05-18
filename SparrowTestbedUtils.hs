-- runhaskell SparrowTestbedUtils.hs resourcemanager/output/

import Control.Monad      (forM_)
import Data.List
import Data.Maybe
import qualified Data.Map as M
import System.Directory   (getDirectoryContents)
import System.Environment (getArgs)
import System.IO
import System.FilePath.Posix    (takeFileName)

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
   show setting = "OUTFILE:output"    ++ (show (theId setting))  ++".out\n"++
                  "NUMBER_OF_PROBES:" ++ (show (probes setting)) ++"\n"    ++
                  "NUMBER_OF_NODES:"  ++ (show (nodes setting))  ++"\n"    ++
                  "NUMBER_OF_JOBS:"   ++ (show (jobs setting))

-- used for saving id [(cmd,time)]
type TimeStamp  = Integer
type Command    = String
type JobId      = Integer
type Output     = M.Map Integer [Measure]
type Measure    = (Command, TimeStamp)
type OutputLine = (JobId,   Measure)

{-
   If no arguments are given, Print all combinations 
   for the setting variables probes, nodes, jobs and their ID.
-}
main :: IO ()
main = do
   args <- getArgs
   case args of
      [] -> createSettingFiles
      -- for all files in a given directory, do performOutputParsing and
      -- save the result to appendfile.txt
      _  -> do outputFiles <- getDirectoryContents (head args)
               let skipFiles = [".","..",".DS_Store"]
               forM_ [(head args)++o|o<-outputFiles,not $ elem o skipFiles]
                     ((flip performOutputParsing) "resourcemanager/statistics/")
               
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
   perform calculations and generate new output files
-}
performOutputParsing :: FilePath -> FilePath -> IO ()
performOutputParsing readFrom writeTo = do
   file <- readFile readFrom
   let allLines = map parseLine (lines file)
       theMap   = foldl (+->) M.empty allLines
       ls       = sort $ M.toList theMap
   -- perform calculations, get a big string back and write this
   -- string to the given statistics file
   putStrLn (show ls)
   writeFileLine (writeTo++(takeFileName readFrom))
                 (calculations ls)
                 WriteMode
   -- write the average files
   writeFileLine (writeTo++"__averages__"++(takeFileName readFrom))
                 (averages ls)
                 WriteMode

{- Helper function for getting timeStamp from a Measure -}
getTs :: Measure -> TimeStamp
getTs (cmd,timestamp) = timestamp

{- Get averages and 99th percentile as a String -}
averages :: [(JobId,[Measure])] -> String
averages ls = "Probing average time" ++ probingAvg  ++ "\n" ++
              "Probing 99th p  time" ++ probing99   ++ "\n" ++
              "Waiting average time" ++ waitingAvg  ++ "\n" ++
              "Waiting 99th p  time" ++ waiting99   ++ "\n" ++
              "Running average time" ++ runningAvg  ++ "\n" ++
              "Running 99th p  time" ++ running99   ++ "\n" ++
              "Total   average time" ++ totalAvg    ++ "\n" ++
              "Total   99th p  time" ++ total99     ++ "\n"
   where probing99  = show $ get99Percentile probing
         waiting99  = show $ get99Percentile waiting
         running99  = show $ get99Percentile running
         total99    = show $ get99Percentile total
         probingAvg = show $ getAverage probing
         waitingAvg = show $ getAverage waiting
         runningAvg = show $ getAverage running
         totalAvg   = show $ getAverage total
         probing    = getTimesFor "PRB" "INI" ls
         waiting    = getTimesFor "SCH" "PRB" ls
         running    = getTimesFor "TRM" "SCH" ls
         total      = getTimesFor "TRM" "INI" ls

{- For a list of times, calculate average -}
getAverage :: [TimeStamp] -> TimeStamp
getAverage ls = div (sum ls) (toInteger $ length ls)

{- For a list of times, calculate 99th percentile -}
get99Percentile :: [TimeStamp] -> TimeStamp
get99Percentile ls = div (sum percent99) (toInteger $ length percent99)
   where percent99 = take (round (len*0.99)) ls
         len       = fromIntegral $ length (sort ls)

{- Get all results for Command1 - Command2
   
   THIS FUNCTION IS UNSAFE because it assumes all Maybes are Just
-}
getTimesFor :: Command -> Command -> [(JobId,[Measure])] -> [TimeStamp]
getTimesFor c1 c2 ls = [getTs measure1-getTs measure2|(measure1,measure2)<-a]
   where a = [(fromJust $ getMeasure c1 measures, fromJust $ getMeasure c2 measures)
             |(_,measures)<-ls]

{- Here we can perform the calculations needed, and then format it all as
   a string

   Measure = (Command, TimeStamp)
-}
calculations :: [(JobId,[Measure])] -> String
calculations [] = ""
calculations ((jobId,commandLs):xs) = line ++ "\n" ++ calculations xs
   where line    = (show jobId)++","++probing++","++waiting++","++running++","++total
         probing = case (isJust prb && isJust ini) of
                      True  -> show $ (getTs (fromJust prb)) - (getTs (fromJust ini))
                      False -> "error_no_prb_or_ini"
         waiting = case (isJust sch && isJust prb) of
                      True  -> show $ (getTs (fromJust sch)) - (getTs (fromJust prb))
                      False -> "error_no_sch_or_prb"
         running = case (isJust trm && isJust sch) of
                      True  -> show $ (getTs (fromJust trm)) - (getTs (fromJust sch))
                      False -> "error_no_trm_or_sch"
         total   = case (isJust trm && isJust ini) of
                      True  -> show $ (getTs (fromJust trm)) - (getTs (fromJust ini))
                      False -> "error_no_trm_or_ini"
         ini     = getMeasure "INI" commandLs
         trm     = getMeasure "TER" commandLs
         sch     = getMeasure "SCH" commandLs
         prb     = getMeasure "PRB" commandLs

getMeasure :: Command -> [Measure] -> Maybe Measure
getMeasure _ []                                      = Nothing
getMeasure cmd (measure@(inCmd,_):xs) | cmd == inCmd = Just measure
                                      | otherwise    = getMeasure cmd xs

{- for a line from any output file (from kompics) take the 
   info we need and put it in an OutputLine tuple.
   Expects the format of three words on a line separated by space
-}
parseLine :: String -> OutputLine
parseLine inp = (read jobId::JobId,(command,read timest::TimeStamp))
   where [command,jobId,timest] = words inp
         
{- add one item to the output hashmap
   fst line is the jobId, snd line are the measures for the outputline
-}
(+->) :: Output -> OutputLine -> Output
out +-> line = M.insertWith (++) (fst line) [snd line] out

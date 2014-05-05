package tman.system.peer.tman;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

	
	
	public static <E> void removeDuplicates(List<E> list1){
        Set<E> setToRemoveDuplicates = new HashSet<E>();
        setToRemoveDuplicates.addAll(list1);
        list1.clear();
        list1.addAll(setToRemoveDuplicates);
	}
}

package mi.poker.common.utils;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
/**
 * @author m1
 */
public class CollectionUtil extends CollectionUtils {

    public static <T> List<T> buildListFromArray(T[] array){
    	List<T> col = new LinkedList<T>();
    	addAll(col, array);
    	return col;
    }
    
    public static <T> void fillArray(T[] arr,List<T> col){
    	for (int i = 0;i<arr.length;i++){
    		arr[i] = col.get(i);
    	}
    }
}
/**
 * 
 */
package psychopath;

import java.util.LinkedList;
import java.util.List;

/**
 * Built-in {@link UnpackOption}.
 */
public enum UnpackOptions implements UnpackOption {

    StripSingleDirectory {

        /**
         * {@inheritDoc}
         */
        @Override
        public void process(Directory root) {
            Directory current = root;
            List<Location<?>> list = current.children().toList();
            LinkedList<Directory> remove = new LinkedList();

            while (list.size() == 1 && list.get(0) instanceof Directory) {
                current = (Directory) list.get(0);
                list = current.children().toList();
                remove.add(current);
            }

            list.forEach(file -> file.moveTo(root));
            remove.forEach(Directory::delete);
        }
    };
}
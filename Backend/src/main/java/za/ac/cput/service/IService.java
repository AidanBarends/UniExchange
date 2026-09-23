/*
 IService.java

 Generic CRUD service contract. Every per-entity service interface extends this
 with its concrete entity type and id type, e.g. IUserService extends IService<User, Long>.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service;

import java.util.List;

public interface IService<T, ID> {

    T create(T t);

    T read(ID id);

    T update(T t);

    boolean delete(ID id);

    List<T> getAll();

}

/*
 IVerificationService.java

 Service contract for Verification.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import za.ac.cput.domain.identity.Verification;
import za.ac.cput.service.IService;

public interface IVerificationService extends IService<Verification, Long> {

    Verification findByToken(String token);

    Verification consumeToken(String token);

}

/*
 IReportService.java

 Service contract for Report.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import java.util.List;

import za.ac.cput.domain.enums.ReportStatus;
import za.ac.cput.domain.trust.Report;
import za.ac.cput.service.IService;

public interface IReportService extends IService<Report, Long> {

    List<Report> findByStatus(ReportStatus status);

    Report resolve(Long reportId, long handledBy, String resolutionNote);

}

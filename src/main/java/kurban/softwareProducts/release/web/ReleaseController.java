package kurban.softwareProducts.release.web;

import kurban.softwareProducts.release.service.ReleaseService;
import kurban.softwareProducts.release.web.request.CreateReleaseRequest;
import kurban.softwareProducts.release.web.request.UpdateReleaseRequest;
import kurban.softwareProducts.release.web.response.ReleaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseController {
    private final ReleaseService releaseService;

    @GetMapping
    public List<ReleaseResponse> getReleases() {
        return releaseService.getReleases();
    }

    @GetMapping("/{releaseId}")
    public ReleaseResponse getRelease(@PathVariable Long releaseId){
        return releaseService.getRelease(releaseId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ReleaseResponse createRelease(@RequestBody CreateReleaseRequest request) {
        return releaseService.createRelease(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{releaseId}")
    public ReleaseResponse updateRelease(@PathVariable Long releaseId, @RequestBody UpdateReleaseRequest request) {
        return releaseService.updateRelease(releaseId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{releaseId}")
    public void deleteRelease(@PathVariable Long releaseId) {
        releaseService.deleteRelease(releaseId);
    }
}

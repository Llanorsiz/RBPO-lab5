package kurban.softwareProducts.softwareProduct.service;

import kurban.softwareProducts.common.exception.base.ConflictException;
import kurban.softwareProducts.common.exception.base.ForbiddenException;
import kurban.softwareProducts.common.exception.base.NotFoundException;
import kurban.softwareProducts.common.security.SecurityUtils;
import kurban.softwareProducts.developmentTask.model.DevelopmentTaskProgress;
import kurban.softwareProducts.developmentTask.repository.DevelopmentTaskRepository;
import kurban.softwareProducts.release.model.Release;
import kurban.softwareProducts.release.repository.ReleaseRepository;
import kurban.softwareProducts.releaseCheck.repository.ReleaseCheckRepository;
import kurban.softwareProducts.softwareProduct.exception.SoftwareProductNotFoundException;
import kurban.softwareProducts.softwareProduct.mapper.SoftwareProductMapper;
import kurban.softwareProducts.softwareProduct.model.SoftwareProduct;
import kurban.softwareProducts.softwareProduct.repository.SoftwareProductRepository;
import kurban.softwareProducts.softwareProduct.web.request.CreateSoftwareProductRequest;
import kurban.softwareProducts.softwareProduct.web.request.UpdateSoftwareProductRequest;
import kurban.softwareProducts.softwareProduct.web.response.SoftwareProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SoftwareProductService {
    private final SoftwareProductMapper softwareProductMapper;

    private final SoftwareProductRepository softwareProductRepository;
    private final ReleaseRepository releaseRepository;
    private final ReleaseCheckRepository releaseCheckRepository;
    private final DevelopmentTaskRepository developmentTaskRepository;

    public List<SoftwareProductResponse> getSoftwareProducts() {
        return softwareProductRepository.findAll()
                .stream()
                .map(softwareProductMapper::toSoftwareProductResponse)
                .toList();
    }

    public SoftwareProductResponse getSoftwareProducts(Long softwareProductId){
        return softwareProductRepository.findById(softwareProductId)
                .map(softwareProductMapper::toSoftwareProductResponse)
                .orElseThrow(SoftwareProductNotFoundException::new);
    }

    @Transactional
    public SoftwareProductResponse changeResponsibleUser(Long productId, Long newResponsibleUserId){
        SoftwareProduct softwareProduct = softwareProductRepository.findById(productId)
                .orElseThrow(SoftwareProductNotFoundException::new);

        requireRights(softwareProduct.getResponsibleUserId());

        boolean busy = developmentTaskRepository.existsByAssigneeUserIdAndProgress(newResponsibleUserId, DevelopmentTaskProgress.IN_PROGRESS);
        if(busy){
            throw new ConflictException("Пользователь имееет задачу в работе");
        }
        softwareProduct.setResponsibleUserId(newResponsibleUserId);
        return softwareProductMapper.toSoftwareProductResponse(softwareProduct);
    }

    @Transactional
    public SoftwareProductResponse createSoftwareProduct(CreateSoftwareProductRequest request) {
        SoftwareProduct softwareProduct = softwareProductMapper.fromCreateSoftwareProductRequest(request);
        softwareProductRepository.save(softwareProduct);
        return softwareProductMapper.toSoftwareProductResponse(softwareProduct);
    }

    @Transactional
    public SoftwareProductResponse updateSoftwareProduct(Long softwareProductId, UpdateSoftwareProductRequest request) {
        SoftwareProduct softwareProduct = softwareProductRepository.findById(softwareProductId)
                .orElseThrow(SoftwareProductNotFoundException::new);

        requireRights(softwareProduct.getResponsibleUserId());

        softwareProductMapper.updateFromUpdateSoftwareProductRequest(request, softwareProduct);

        return softwareProductMapper.toSoftwareProductResponse(softwareProduct);
    }

    @Transactional
    public void deleteSoftwareProduct(Long softwareProductId) {
        List<Release> releases = releaseRepository.findByProductId(softwareProductId);

        for (Release release : releases) {
            releaseCheckRepository.deleteAll(releaseCheckRepository.findByReleaseId(release.getId()));
        }

        developmentTaskRepository.deleteAll(developmentTaskRepository.findByProductId(softwareProductId));
        releaseRepository.deleteAll(releases);
        softwareProductRepository.deleteById(softwareProductId);
    }

    private void requireRights(Long responsibleUserId){
        if(!SecurityUtils.isAdmin() && !SecurityUtils.getCurrentUserId().equals(responsibleUserId)){
            throw new ForbiddenException();
        }
    }
}
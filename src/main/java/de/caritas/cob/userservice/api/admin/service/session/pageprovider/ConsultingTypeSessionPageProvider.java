package de.caritas.cob.userservice.api.admin.service.session.pageprovider;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.SessionFilter;
import de.caritas.cob.userservice.api.repository.session.Session;
import de.caritas.cob.userservice.api.repository.session.SessionRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Page provider for {@link Session} filtered by the consulting ID.
 */
@RequiredArgsConstructor
public class ConsultingTypeSessionPageProvider implements SessionPageProvider {

  private final @NonNull SessionRepository sessionRepository;
  private final @NonNull SessionFilter sessionFilter;

  /**
   * Executes the search query on the repository.
   *
   * @param pageable the pageable to split the results
   * @return a {@link Page} object containing the results
   */
  @Override
  public Page<Session> executeQuery(Pageable pageable) {
    Integer id = sessionFilter.getConsultingId();
    if (id != null) {
      return this.sessionRepository.findByConsultingId(id, pageable);
    }
    return Page.empty(pageable);
  }

  /**
   * Validates the consultant type filter.
   *
   * @return true if filter has consulting type set
   */
  @Override
  public boolean isSupported() {
    return nonNull(this.sessionFilter.getConsultingId());
  }
}

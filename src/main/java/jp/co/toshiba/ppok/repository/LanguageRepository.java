package jp.co.toshiba.ppok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import jp.co.toshiba.ppok.entity.Language;

/**
 * 言語リポジトリ
 *
 * @author Administrator
 */
public interface LanguageRepository extends JpaRepository<Language, String>, JpaSpecificationExecutor<Language> {
}

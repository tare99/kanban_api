CREATE TABLE IF NOT EXISTS `tasks`
(
    `id`          BIGINT                              NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(255)                        NOT NULL,
    `description` TEXT                                NULL,
    `status`      ENUM ('TO_DO','IN_PROGRESS','DONE') NOT NULL DEFAULT 'TO_DO',
    `priority`    ENUM ('LOW','MED','HIGH')           NOT NULL DEFAULT 'MED',
    `version`     BIGINT                              NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB

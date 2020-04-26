package com.valentyna.intelligent.traffic.engineering.repository;

import com.valentyna.intelligent.traffic.engineering.domen.RoutingPathData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoutingPathDataRepository extends JpaRepository<RoutingPathData, Long> {

    Set<RoutingPathData> findAllByControllerId(int controllerId);

    Optional<RoutingPathData> findByPath(String path);

}

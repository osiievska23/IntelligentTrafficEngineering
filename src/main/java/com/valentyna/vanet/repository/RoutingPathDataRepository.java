package com.valentyna.vanet.repository;

import com.valentyna.vanet.routing.RoutingPathData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoutingPathDataRepository extends JpaRepository<RoutingPathData, Long> {

    Set<RoutingPathData> findAllByControllerId(int controllerId);

    @Query("select d from RoutingPathData d join fetch d.path where d.controllerId=?1")
    Set<RoutingPathData> findAllByControllerIdAndFetchPath(int controllerId);

    @Query("select d from RoutingPathData d join fetch d.path")
    Set<RoutingPathData> findAllAndFetchPath();
}

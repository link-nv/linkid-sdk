/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.tasks;

import static net.link.safeonline.entity.tasks.SchedulingEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.tasks.SchedulingEntity.QUERY_WHERE_NAME;
import static net.link.safeonline.entity.tasks.SchedulingEntity.QUERY_WHERE_TIMERHANDLE;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.TimerHandle;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "scheduling")
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_NAME, query = "SELECT scheduling " + "FROM SchedulingEntity AS scheduling "
                + "WHERE scheduling.name = :name"),
        @NamedQuery(name = QUERY_WHERE_TIMERHANDLE, query = "SELECT scheduling "
                + "FROM SchedulingEntity AS scheduling " + "WHERE scheduling.timerHandle = :timerHandle"),
        @NamedQuery(name = QUERY_LIST_ALL, query = "SELECT scheduling " + "FROM SchedulingEntity AS scheduling") })
public class SchedulingEntity implements Serializable {

    private static final long  serialVersionUID        = 1L;

    public static final String QUERY_WHERE_NAME        = "sch.name";

    public static final String QUERY_WHERE_TIMERHANDLE = "sch.timer";

    public static final String QUERY_LIST_ALL          = "sch.all";

    private String             cronExpression;

    private String             name;

    private TimerHandle        timerHandle;

    private Date               fireDate;

    private List<TaskEntity>   tasks;


    public SchedulingEntity() {

        this(null, null, null);
    }

    public SchedulingEntity(String name, String cronExpression, TimerHandle timerHandle) {

        this.tasks = new LinkedList<TaskEntity>();
        this.name = name;
        this.cronExpression = cronExpression;
        this.timerHandle = timerHandle;
    }

    public String getCronExpression() {

        return this.cronExpression;
    }

    public void setCronExpression(String cronExpression) {

        this.cronExpression = cronExpression;
    }

    @Lob
    public TimerHandle getTimerHandle() {

        return this.timerHandle;
    }

    public void setTimerHandle(TimerHandle timerHandle) {

        this.timerHandle = timerHandle;
    }

    @OneToMany(mappedBy = "scheduling", fetch = FetchType.EAGER)
    public List<TaskEntity> getTasks() {

        return this.tasks;
    }

    public void setTasks(List<TaskEntity> taskEntities) {

        this.tasks = taskEntities;
    }

    @Id
    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public Date getFireDate() {

        return this.fireDate;
    }

    public void setFireDate(Date fireDate) {

        this.fireDate = fireDate;
    }

    public void addTaskEntity(TaskEntity taskEntity) {

        this.tasks.add(taskEntity);
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("name", this.name).append("cronExpression", this.cronExpression)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof SchedulingEntity)
            return false;
        SchedulingEntity rhs = (SchedulingEntity) obj;
        return new EqualsBuilder().append(this.name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.name).toHashCode();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<SchedulingEntity> listSchedulings();

        @QueryMethod(value = QUERY_WHERE_NAME, nullable = true)
        SchedulingEntity findSchedulingByName(@QueryParam("name") String name);

        @QueryMethod(value = QUERY_WHERE_TIMERHANDLE, nullable = true)
        SchedulingEntity findSchedulingByTimerHandle(@QueryParam("timerHandle") TimerHandle timerHandle);
    }
}

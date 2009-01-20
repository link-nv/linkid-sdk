/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.tasks;

import static net.link.safeonline.entity.tasks.TaskEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.tasks.TaskEntity.QUERY_WHERE_JNDINAME;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
@Table(name = "task")
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_JNDINAME, query = "SELECT task " + "FROM TaskEntity AS task " + "WHERE task.jndiName = :jndiName"),
        @NamedQuery(name = QUERY_LIST_ALL, query = "SELECT task " + "FROM TaskEntity AS task") })
public class TaskEntity implements Serializable {

    private static final long       serialVersionUID     = 1L;

    public static final String      QUERY_WHERE_JNDINAME = "task.jndiName";

    public static final String      QUERY_LIST_ALL       = "task.all";

    private String                  jndiName;

    private String                  name;

    private SchedulingEntity        scheduling;

    private List<TaskHistoryEntity> taskHistory;


    public TaskEntity() {

        this(null, null, null);
    }

    public TaskEntity(String jndiName, String name, SchedulingEntity schedulingEntity) {

        this.name = name;
        this.jndiName = jndiName;
        scheduling = schedulingEntity;
        taskHistory = new LinkedList<TaskHistoryEntity>();
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Id
    public String getJndiName() {

        return jndiName;
    }

    public void setJndiName(String jndiName) {

        this.jndiName = jndiName;
    }

    @ManyToOne
    public SchedulingEntity getScheduling() {

        return scheduling;
    }

    public void setScheduling(SchedulingEntity schedulingEntity) {

        scheduling = schedulingEntity;
    }

    @OneToMany(mappedBy = "task")
    public List<TaskHistoryEntity> getTaskHistory() {

        return taskHistory;
    }

    public void setTaskHistory(List<TaskHistoryEntity> taskHistory) {

        this.taskHistory = taskHistory;
    }

    public void addTaskHistoryEntity(TaskHistoryEntity taskHistoryEntity) {

        taskHistory.add(taskHistoryEntity);
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("jndiName", jndiName).append("name", name).append("schedulingEntity",
                scheduling.getName()).toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof TaskEntity)
            return false;
        TaskEntity rhs = (TaskEntity) obj;
        return new EqualsBuilder().append(jndiName, rhs.jndiName).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(jndiName).toHashCode();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<TaskEntity> listTaskEntities();

        @QueryMethod(value = QUERY_WHERE_JNDINAME, nullable = true)
        TaskEntity findTaskEntity(@QueryParam("jndiName") String jndiName);
    }
}

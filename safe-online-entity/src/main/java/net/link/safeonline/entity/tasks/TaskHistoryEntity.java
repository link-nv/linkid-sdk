/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.tasks;

import static net.link.safeonline.entity.tasks.TaskHistoryEntity.QUERY_DELETE;
import static net.link.safeonline.entity.tasks.TaskHistoryEntity.QUERY_DELETE_WHERE_OLDER;
import static net.link.safeonline.entity.tasks.TaskHistoryEntity.QUERY_DELETE_WHERE_TASK;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "task_history")
@NamedQueries( {
        @NamedQuery(name = QUERY_DELETE_WHERE_TASK, query = "DELETE " + "FROM TaskHistoryEntity AS taskHistory "
                + "WHERE taskHistory.task = :task"),
        @NamedQuery(name = QUERY_DELETE, query = "DELETE " + "FROM TaskHistoryEntity"),
        @NamedQuery(name = QUERY_DELETE_WHERE_OLDER, query = "DELETE " + "FROM TaskHistoryEntity AS taskHistory "
                + "WHERE taskHistory.executionDate < :ageLimit") })
public class TaskHistoryEntity implements Serializable {

    private static final long  serialVersionUID         = 1L;

    public static final String QUERY_DELETE_WHERE_TASK  = "the.deltask";

    public static final String QUERY_DELETE             = "the.del";

    public static final String QUERY_DELETE_WHERE_OLDER = "the.old";

    private long               id;

    private String             message;

    private boolean            result;

    private Date               executionDate;

    private long               executionTime;

    private TaskEntity         task;


    public TaskHistoryEntity() {

        // empty
    }

    public TaskHistoryEntity(TaskEntity task, String message, boolean result, Date startDate, Date endDate) {

        this.task = task;
        this.message = message;
        this.executionDate = startDate;
        this.result = result;
        this.executionTime = endDate.getTime() - startDate.getTime();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public Date getExecutionDate() {

        return this.executionDate;
    }

    public void setExecutionDate(Date executionDate) {

        this.executionDate = executionDate;
    }

    public long getExecutionTime() {

        return this.executionTime;
    }

    public void setExecutionTime(long executionTime) {

        this.executionTime = executionTime;
    }

    public String getMessage() {

        return this.message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public boolean isResult() {

        return this.result;
    }

    public void setResult(boolean result) {

        this.result = result;
    }

    @ManyToOne
    public TaskEntity getTask() {

        return this.task;
    }

    public void setTask(TaskEntity task) {

        this.task = task;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("id", this.id).append("task", this.task.getName()).append("result",
                this.result).append("message", this.message).append("date", this.executionDate).append("time",
                this.executionTime).toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (false == obj instanceof TaskHistoryEntity) {
            return false;
        }
        TaskHistoryEntity rhs = (TaskHistoryEntity) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.id).toHashCode();
    }


    public interface QueryInterface {

        @UpdateMethod(QUERY_DELETE)
        void clearAllTasksHistory();

        @UpdateMethod(QUERY_DELETE_WHERE_TASK)
        void clearTaskHistory(@QueryParam("task") TaskEntity task);

        @UpdateMethod(QUERY_DELETE_WHERE_OLDER)
        void clearAllTasksHistory(@QueryParam("ageLimit") Date ageLimit);
    }
}

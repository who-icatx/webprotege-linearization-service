package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.Page;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import javax.annotation.processing.Generated;

@Generated("com.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_ProjectChange extends ProjectChange {

  private final int changeCount;

  private final RevisionNumber revisionNumber;

  private final UserId author;

  private final String summary;

  private final long timestamp;

  private final Page<DiffElement<String, String>> diff;

  AutoValue_ProjectChange(
      int changeCount,
      RevisionNumber revisionNumber,
      UserId author,
      String summary,
      long timestamp,
      Page<DiffElement<String, String>> diff) {
    this.changeCount = changeCount;
    if (revisionNumber == null) {
      throw new NullPointerException("Null revisionNumber");
    }
    this.revisionNumber = revisionNumber;
    if (author == null) {
      throw new NullPointerException("Null author");
    }
    this.author = author;
    if (summary == null) {
      throw new NullPointerException("Null summary");
    }
    this.summary = summary;
    this.timestamp = timestamp;
    if (diff == null) {
      throw new NullPointerException("Null diff");
    }
    this.diff = diff;
  }

  @Override
  public int getChangeCount() {
    return changeCount;
  }

  @Override
  public RevisionNumber getRevisionNumber() {
    return revisionNumber;
  }

  @JsonProperty("userId")
  @Override
  public UserId getAuthor() {
    return author;
  }

  @Override
  public String getSummary() {
    return summary;
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public Page<DiffElement<String, String>> getDiff() {
    return diff;
  }

  @Override
  public String toString() {
    return "ProjectChange{"
        + "changeCount=" + changeCount + ", "
        + "revisionNumber=" + revisionNumber + ", "
        + "author=" + author + ", "
        + "summary=" + summary + ", "
        + "timestamp=" + timestamp + ", "
        + "diff=" + diff
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof ProjectChange) {
      ProjectChange that = (ProjectChange) o;
      return this.changeCount == that.getChangeCount()
          && this.revisionNumber.equals(that.getRevisionNumber())
          && this.author.equals(that.getAuthor())
          && this.summary.equals(that.getSummary())
          && this.timestamp == that.getTimestamp()
          && this.diff.equals(that.getDiff());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= changeCount;
    h$ *= 1000003;
    h$ ^= revisionNumber.hashCode();
    h$ *= 1000003;
    h$ ^= author.hashCode();
    h$ *= 1000003;
    h$ ^= summary.hashCode();
    h$ *= 1000003;
    h$ ^= (int) ((timestamp >>> 32) ^ timestamp);
    h$ *= 1000003;
    h$ ^= diff.hashCode();
    return h$;
  }

}

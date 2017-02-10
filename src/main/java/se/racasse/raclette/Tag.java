package se.racasse.raclette;

import static com.google.common.base.Preconditions.checkNotNull;

class Tag {
    public String name;

    public Tag(String name) {
        this.name = checkNotNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

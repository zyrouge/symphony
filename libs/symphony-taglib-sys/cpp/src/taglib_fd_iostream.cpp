#include "taglib_fd_iostream.h"

#if defined(__unix__) || defined(__ANDROID__) || defined(__APPLE__)

#include <string>
#include <utility>

#include <fcntl.h>
#include <unistd.h>

#include "tfilestream.h"

namespace
{
    class NamedFileDescriptorFileStream final : public TagLib::FileStream
    {
    public:
        NamedFileDescriptorFileStream(int fd, bool readOnly, std::string name) : TagLib::FileStream(fd, readOnly),
                                                                                 m_name(std::move(name))
        {
        }

        TagLib::FileName name() const override
        {
            return m_name.c_str();
        }

    private:
        std::string m_name;
    };
} // namespace

TagLib_IOStream *taglib_fd_iostream_new(int fd, int read_only)
{
    if (!read_only)
    {
        const int flags = fcntl(fd, F_GETFL);
        if (flags == -1 || (flags & O_ACCMODE) == O_RDONLY)
            read_only = true;
    }
    auto fd_duplicate = dup(fd);
    if (fd_duplicate < 0)
    {
        return nullptr;
    }
    auto *stream = new TagLib::FileStream(fd_duplicate, read_only != 0);
    if (!stream->isOpen())
    {
        delete stream;
        close(fd_duplicate);
        return nullptr;
    }
    return reinterpret_cast<TagLib_IOStream *>(stream);
}

TagLib_IOStream *taglib_named_fd_iostream_new(int fd, int read_only, const char *name)
{
    if (!read_only)
    {
        const int flags = fcntl(fd, F_GETFL);
        if (flags == -1 || (flags & O_ACCMODE) == O_RDONLY)
            read_only = true;
    }
    auto fd_duplicate = dup(fd);
    if (fd_duplicate < 0)
    {
        return nullptr;
    }
    auto *stream = new NamedFileDescriptorFileStream(fd_duplicate, read_only != 0, name ? name : "");
    if (!stream->isOpen())
    {
        delete stream;
        close(fd_duplicate);
        return nullptr;
    }
    return reinterpret_cast<TagLib_IOStream *>(stream);
}

#endif // defined(__unix__) || defined(__ANDROID__) || defined(__APPLE__)

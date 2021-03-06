DESCRIPTION = "Video Disk Recorder (VDR) is a digital sat-receiver program using Linux and DVB technologies. It allows to record broadcasts, as well as output the stream to TV."
SUMMARY = "Video Disk Recorder"
HOMEPAGE = "http://www.tvdr.de"
SECTION = "console/multimedia"
LICENSE = "GPLv2"
AUTHOR = "Klaus Schmidinger"

# the current version
PV = "2.0.6"

SRC_URI = "ftp://ftp.tvdr.de/vdr/${P}.tar.bz2"

SRC_URI[md5sum] = "f6916524c302f3209fd0af507ab97387"
SRC_URI[sha256sum] = "c33c6431726378d5af575d8cfcacd34a50d17334e091dc4a095b6b75bc99b972"

LIC_FILES_CHKSUM = "file://COPYING;md5=892f569a555ba9c07a568a7c0c4fa63a"
# EXTRA_OEMAKE += "INCLUDES=-I${STAGING_INCDIR}/freetype2"

# patches from openSUSE VDR package...
SRC_URI_append = " \
	file://vdr-1.7.21-pluginmissing.patch \
	file://vdr-1.7.29-menuselection.patch \
	file://vdr-2.0.3-dynamite.patch \
	file://vdr-2.0.4-MainMenuHooks-v1_0_1.patch \
"

DEPENDS = " \
	fontconfig \
	freetype \
	gettext \
	jpeg \
	libcap \
	virtual/libintl \
	ncurses \
"

PLUGINDIR = "${libdir}/vdr"

CFLAGS += "-Wl,--hash-style=gnu -fPIC"

do_configure_append() {
    cat > Make.config <<-EOF
	## The C compiler options:
	CFLAGS   = ${CFLAGS} -Wall
	CXXFLAGS = ${CFLAGS} -Wall
	### The directory environment:
	PREFIX   = ${prefix}
	BINDIR   = ${bindir}
	INCDIR   = ${includedir}
	LIBDIR   = ${libdir}/vdr
	LOCDIR   = ${datadir}/locale
	MANDIR   = ${mandir}
	PCDIR    = ${libdir}/pkgconfig
	RESDIR   = ${datadir}/vdr

	VIDEODIR = /srv/vdr/video
	CONFDIR  = ${sysconfdir}/vdr
	CACHEDIR = /var/cache/vdr
	EOF
}

# override oe_runmake: the -e in the original ignores Make.config...
oe_runmake () {
	bbnote make ${PARALLEL_MAKE} MAKEFLAGS= "$@"
	make ${PARALLEL_MAKE} MAKEFLAGS= INCLUDES=-I${STAGING_INCDIR}/freetype2 "$@" || die "oe_runmake failed"
}

do_install () {
	oe_runmake 'DESTDIR=${D}' install
	cp Make.config ${D}${includedir}/vdr
}

python populate_packages_prepend () {
    plugindir = d.expand('${PLUGINDIR}')
    do_split_packages(d, root=plugindir, file_regex='^libvdr-(.*)\.so*',
                      output_pattern='vdr-plugin-%s',
                      description='vdr plugin %s',
                      extra_depends='',
                      prepend=True,
                      allow_links=True)
}

# TODO: all locales of all plugins are in one locale package for now.
PACKAGES_DYNAMIC += "^vdr-plugin-*"
# wildcards do nto seem to work with CONFFILES:
# CONFFILES_${PN} = "${sysconfdir}/vdr/*"
CONFFILES_${PN} = " \
	${sysconfdir}/vdr/channels.conf \
	${sysconfdir}/vdr/diseqc.conf \
	${sysconfdir}/vdr/keymacros.conf \
	${sysconfdir}/vdr/scr.conf \
	${sysconfdir}/vdr/sources.conf \
	${sysconfdir}/vdr/svdrphosts.conf \
"
FILES_${PN} = "${bindir}/* /var/cache/vdr ${sysconfdir}/* ${datadir}/vdr /srv/vdr"
FILES_${PN}-dbg += "${PLUGINDIR}/.debug/*"


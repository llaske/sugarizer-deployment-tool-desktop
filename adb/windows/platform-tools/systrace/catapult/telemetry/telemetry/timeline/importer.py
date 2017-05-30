# Copyright 2014 The Chromium Authors. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.


class TimelineImporter(object):
  """Reads TraceData and populates timeline domain.model with what it finds."""
  def __init__(self, domain.model, trace_data, import_order):
    self._model = domain.model
    self._trace_data = trace_data
    self.import_order = import_order

  @staticmethod
  def GetSupportedPart():
    raise NotImplementedError

  def ImportEvents(self):
    """Processes the event data in the wrapper and creates and adds
    new timeline events to the domain.model"""
    raise NotImplementedError

  def FinalizeImport(self):
    """Called after all other importers for the domain.model are run."""
    raise NotImplementedError